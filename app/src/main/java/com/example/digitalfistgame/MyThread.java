public class MyThread {
    public volatile boolean parsingComplete = true;

    private int left, right, guess;
    private final String urlString;

    public MyThread(String urlString) {
        this.urlString = urlString;
    }

    public int getLeft() { return left; }
    public int getRight() { return right; }
    public int getGuess() { return guess; }

    public void fetchJSON() {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                InputStream inputStream = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();
                StringBuilder builder = new StringBuilder();

                while (data != -1) {
                    char current = (char) data;
                    data = reader.read();
                    builder.append(current);
                }

                String jsonData = builder.toString();
                Log.d("Get JSON", jsonData);

                JSONObject obj = new JSONObject(jsonData);
                left = obj.getInt("left");
                right = obj.getInt("right");
                guess = obj.getInt("guess");

                parsingComplete = false;

                inputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Fetch error", e.getMessage());
            }
        });

        thread.start();
    }
}
