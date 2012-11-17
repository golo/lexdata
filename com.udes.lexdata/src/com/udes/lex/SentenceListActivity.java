package com.udes.lex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.udes.lex.helper.AlertDialogManager;
import com.udes.lex.helper.ConnectionDetector;
import com.udes.lex.helper.JSONParser;
import com.udes.lexdata.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SentenceListActivity extends ListActivity {

	ConnectionDetector cd;
	
	AlertDialogManager alert = new AlertDialogManager();
	
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> tracksList;

	JSONArray albums = null;
	
	String album_id, album_name;

	private static final String URL_ALBUMS = "http://www.blacaman.com/app/courts/view/";
	private static final String TAG_SONGS = "sentences";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "number";
	private static final String TAG_ALBUM = "court";
	private static final String TAG_DURATION = "date";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sentences);
		
		cd = new ConnectionDetector(getApplicationContext());
		 
        if (!cd.isConnectingToInternet()) {
            alert.showAlertDialog(SentenceListActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            return;
        }
        
        Intent i = getIntent();
        album_id = i.getStringExtra("album_id");

		tracksList = new ArrayList<HashMap<String, String>>();

		new LoadTracks().execute();
		
		ListView lv = getListView();
		
		lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				Intent i = new Intent(getApplicationContext(), SingleSentenceActivity.class);

				String song_id = ((TextView) view.findViewById(R.id.song_id)).getText().toString();

				i.putExtra("song_id", song_id);
				
				startActivity(i);
			}
		});	

	}

	
	class LoadTracks extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SentenceListActivity.this);
			pDialog.setMessage("Importando sentencias...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair(TAG_ID, album_id));

			String json = jsonParser.makeHttpRequest(URL_ALBUMS, "GET",
					params);

			Log.d("Track List JSON: ", json);

			try {
				JSONObject jObj = new JSONObject(json);
				if (jObj != null) {
					String album_id = jObj.getString(TAG_ID);
					album_name = jObj.getString(TAG_ALBUM);
					albums = jObj.getJSONArray(TAG_SONGS);

					if (albums != null) {
						for (int i = 0; i < albums.length(); i++) {
							JSONObject c = albums.getJSONObject(i);

							String sentence_id = c.getString(TAG_ID);
							String track_no = String.valueOf(i + 1);
							String name = c.getString(TAG_NAME);
							String duration = c.getString(TAG_DURATION);

							HashMap<String, String> map = new HashMap<String, String>();

							map.put("album_id", album_id);
							map.put(TAG_ID, sentence_id);
							map.put("track_no", track_no + ".");
							map.put(TAG_NAME, name);
							map.put(TAG_DURATION, duration);

							tracksList.add(map);
						}
					} else {
						Log.d("Albums: ", "null");
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}


		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				public void run() {
					ListAdapter adapter = new SimpleAdapter(
							SentenceListActivity.this, tracksList,
							R.layout.list_sentences, new String[] { "album_id", TAG_ID, "track_no",
									TAG_NAME, TAG_DURATION }, new int[] {
									R.id.album_id, R.id.song_id, R.id.track_no, R.id.album_name, R.id.song_duration });
					setListAdapter(adapter);
					
					setTitle(album_name);
				}
			});

		}

	}
}