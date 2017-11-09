/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kis.hatebucount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author naoki
 */
public class HatebuRank {
    
    static class Ranking {
        int rank;
        int count;

        public Ranking(int rank, int count) {
            this.rank = rank;
            this.count = count;
        }
        
    }
    
    public static void main(String[] args) throws MalformedURLException, IOException {
        Pattern pattern = Pattern.compile("data-entryrank=\"(\\d+)\" data-bookmark-count=\"(\\d+)\"");

        for (LocalDate date = LocalDate.of(2017, 10, 1);; date = date.minusMonths(1)) {
            int year = date.getYear();
            int month = date.getMonthValue();
            String url = String.format("http://b.hatena.ne.jp/ranking/monthly/%04d%02d", date.getYear(), date.getMonthValue());
            URLConnection conn = new URL(url).openConnection();
            try (InputStream is = conn.getInputStream();
                    BufferedReader bur = new BufferedReader(new InputStreamReader(is))) {
                
                List<Ranking> ranks = bur.lines()
                        .filter(line -> line.contains("hb-entry-unit-with-favorites"))
                        .map(pattern::matcher)
                        .filter(Matcher::find)
                        .map(mat -> new Ranking(Integer.parseInt(mat.group(1)), Integer.parseInt(mat.group(2))))
                        .collect(Collectors.toList());
                System.out.printf("%04d/%02d", year, month);
                int prerank = 0;
                for (Ranking rank : ranks) {
                    if (prerank > rank.rank) {
                        break;
                    }
                    prerank = rank.rank;
                    System.out.print("\t" + rank.count);
                }
                System.out.println();
            }
                    
        }
    }
}
