package main.engines;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class IndexationEngine extends Thread{

    public IndexationEngine() { }

    public IndexationEngine(String url, Integer siteId) {
        this.url = url;
        this.siteId = siteId;
    }

    private String url;
    private Integer siteId;

    @Override
    public void run() {


        try {
            URL uri = new URL("http://localhost:8080/indexPages");
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();

            connection.setRequestMethod("POST");
            Map<String, String> params = new HashMap<>();
            params.put("url", url);
            params.put("siteIdI", siteId.toString());

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }

            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            connection.setDoOutput(true);
            try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
                writer.write(postDataBytes);
                writer.flush();
                writer.close();

                StringBuilder content;

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    content = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                        content.append(System.lineSeparator());
                    }
                }
                System.out.println(content.toString());
            } finally {
                connection.disconnect();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }


    }
}
