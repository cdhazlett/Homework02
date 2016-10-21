package edu.calvin.cs262.lab06;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reads Monopoly player data from a REST api
 *
 * for CS 262, homework 02
 *
 * @author Christiaan Hazlett
 * @version Fall, 2016
 */

public class MainActivity extends AppCompatActivity {

    private EditText playerText;
    private Button fetchButton;

    private ListView itemsListView;

    private List<Player> playerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerText= (EditText) findViewById(R.id.playerText);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        itemsListView = (ListView) findViewById(R.id.playerListView);


        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(playerText);

                new GetPlayerTask().execute(createURL());
            }
        });
        Log.d("Debug", "Test1");
    }


    /**
     * Formats a URL for downloading either a single player's data, or all players' data
     *
     * @return URL formatted for Monopoly player server
     */
    private URL createURL() {

        // Create the URL to load the data from the API
        if (playerText.length() < 1) {
            try {
                return new URL("http://cs262.cs.calvin.edu:8089/monopoly/players");
            }
            catch (Exception e){
                Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                Log.d("Debug", "Loc 1");
            }
        }
        else {
            try {
                return new URL("http://cs262.cs.calvin.edu:8089/monopoly/player/" + playerText.getText());
            }
            catch (Exception e){
                Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                Log.d("Debug", "Loc 2");
            }
        }

        return null;
    }

    /**
     * Deitel's method for programmatically dismissing the keyboard.
     *
     * @param view the TextView currently being edited
     */
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Inner class for GETing the player data from the API
     */
    private class GetPlayerTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return new JSONObject(result.toString());
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject player_data) {
            if (player_data != null) {
                convertJSONtoArrayList(player_data);
                MainActivity.this.updateDisplay();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Convert the JSON player data to an ArrayList
     *
     * @param player_data
     */
    private void convertJSONtoArrayList(JSONObject player_data) {

        playerList.clear(); // clear old weather data
        try {
            if (!player_data.has("plist")) {

                int player_id = player_data.getInt("id");
                String player_name = "";
                if (player_data.has("name")) player_name = player_data.getString("name");
                String player_email = player_data.getString("emailaddress");

                String playerLineText = player_id + ", " + player_name + ", " + player_email;

                playerList.add(new Player(playerLineText));

            }
            else {
                JSONArray list = player_data.getJSONArray("plist");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject player = list.getJSONObject(i);

                    int player_id = player.getInt("id");
                    String player_name = player.getString("name");
                    String player_email = player.getString("emailaddress");

                    String playerLineText = player_id + ", " + player_name + ", " + player_email;

                    playerList.add(new Player(playerLineText));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the player data on the forecast ListView through a simple adapter
     */
    private void updateDisplay() {
        if (playerList== null) {
            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Player item : playerList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("playerText", item.getPlayerText());
            data.add(map);
        }

        int resource = R.layout.player_item;
        String[] from = {"playerText"};
        int[] to = {R.id.playerItemText};

        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        itemsListView.setAdapter(adapter);
    }

}
