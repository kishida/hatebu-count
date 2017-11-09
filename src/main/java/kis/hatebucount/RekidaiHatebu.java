package kis.hatebucount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author naoki
 */
public class RekidaiHatebu {
    public static void main(String[] args) throws MalformedURLException, IOException {
                Pattern pattern = Pattern.compile("<em><a href=\"([^\"]+)\"");

        for (LocalDate date = LocalDate.of(2010, 12, 1); date.isAfter(LocalDate.of(2004, 12, 1)); date = date.minusMonths(1)) {
            int year = date.getYear();
            int month = date.getMonthValue();
            String url = String.format("http://ebi.dyndns.biz/hateburanking/%04d/%02d/", year, month);
            URLConnection conn = new URL(url).openConnection();
            try (InputStream is = conn.getInputStream();
                    BufferedReader bur = new BufferedReader(new InputStreamReader(is));
                    Stream<String> lines = bur.lines()) {
                List<String> urls = lines.map(pattern::matcher)
                        .filter(Matcher::find)
                        .map(mat -> mat.group(1))
                        .collect(Collectors.toList());
                System.out.printf("%04d/%02d\t%s%n", year, month, getBookmarkCount(urls).stream().map(String::valueOf).collect(Collectors.joining("\t")));
            }
        }
    }
    
    static List<Integer> getBookmarkCount(List<String> urls) throws MalformedURLException, IOException {
        Pattern pattern = Pattern.compile("\"([^\"]+)\":(\\d+)");
        
        URL u = new URL("http://api.b.st-hatena.com/entry.counts?"
                + urls.stream()
                    .limit(50)
                    .map(RekidaiHatebu::encode)
                    .map(s -> "url=" + s)
                    .collect(Collectors.joining("&")));
        URLConnection conn = u.openConnection();
        try (InputStream is = conn.getInputStream();
                BufferedReader bur = new BufferedReader(new InputStreamReader(is));
                Stream<String> lines = bur.lines()) {
            String json = lines.findFirst().orElseThrow(RuntimeException::new);
            
            Matcher matcher = pattern.matcher(json);
            Map<String, Integer> result = new HashMap<>();
            for (int idx = 0; matcher.find(idx);) {
                result.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
                idx = matcher.end(0);
            }
            return urls.stream().map(result::get).collect(Collectors.toList());
        }
    }
    
    static String encode(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
