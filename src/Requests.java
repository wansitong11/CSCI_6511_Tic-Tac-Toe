import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.HashMap;


public class Requests {
    private final String BASE_URL = "http://www.notexponential.com/aip2pgaming/api/index.php";
    private HashMap<String, String> properties;

    public Requests() {
        properties = new HashMap<String, String>();
        properties.put("x-api-key", "402f27e625a2c34a2599");
        properties.put("userid", "1110");
        properties.put("Content-Type", "application/x-www-form-urlencoded");
    }

    /**
     * Send request with GET
     *
     * @param params
     * @return
     */
    public String get(HashMap<String, String> params) throws IOException {
        StringBuilder result = new StringBuilder();

        // Add parameters
        String concatUrl = BASE_URL + "?";
        StringBuilder paramsSB = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsSB.append("&");
            paramsSB.append(entry.getKey());
            paramsSB.append("=");
            paramsSB.append(entry.getValue());
        }
        concatUrl += paramsSB.toString().substring(1);

        // Create connection
        URL url = new URL(concatUrl);
        URLConnection connection = url.openConnection();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.connect();

        // Read result
        try {
            BufferedReader bufferIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (true) {
                String temp = bufferIn.readLine();
                if (temp == null) {
                    break;
                } else {
                    result.append(temp);
                }
            }
        } catch (Exception e) {

        }

        return result.toString();
    }

    /**
     * Send request with POST
     *
     * @param params
     * @return
     */
    public String post(HashMap<String, String> params) throws IOException {
        StringBuilder result = new StringBuilder();

        // Create connection
        URL url = new URL(BASE_URL);
        URLConnection connection = url.openConnection();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // Add parameters
        StringBuilder paramsSB = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsSB.append("&");
            paramsSB.append(entry.getKey());
            paramsSB.append("=");
            paramsSB.append(entry.getValue());
        }
        OutputStream outputStream = connection.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        bufferedWriter.write(paramsSB.toString().substring(1));
        bufferedWriter.flush();

        // Read result
        try {
            BufferedReader bufferIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (true) {
                String temp = bufferIn.readLine();
                if (temp == null) {
                    break;
                } else {
                    result.append(temp);
                }
            }
        } catch (Exception e) {

        }

        return result.toString();
    }
}