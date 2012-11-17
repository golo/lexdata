package com.udes.lex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.udes.lex.helper.AlertDialogManager;
import com.udes.lex.helper.ConnectionDetector;
import com.udes.lex.helper.JSONParser;
import com.udes.lexdata.R;

public class CourtsActivity extends ListActivity {
	ConnectionDetector cd;
	
	AlertDialogManager alert = new AlertDialogManager();
	
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> albumsList;

	JSONArray albums = null;

	private static final String URL_ALBUMS = "http://www.blacaman.com/app/courts/index.json";

	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "court";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courts);
		
		cd = new ConnectionDetector(getApplicationContext());
		 
        if (!cd.isConnectingToInternet()) {
            alert.showAlertDialog(CourtsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            return;
        }

		albumsList = new ArrayList<HashMap<String, String>>();

		new LoadAlbums().execute();
		
		ListView lv = getListView();
		
		lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				Intent i = new Intent(getApplicationContext(), SentenceListActivity.class);
				
				String album_id = ((TextView) view.findViewById(R.id.album_id)).getText().toString();
				i.putExtra("album_id", album_id);				
				
				startActivity(i);
			}
		});		
	}

	class LoadAlbums extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CourtsActivity.this);
			pDialog.setMessage("Importando datos...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			String json = jsonParser.makeHttpRequest(URL_ALBUMS, "GET",
					params);

			Log.d("Albums JSON: ", "> " + json);

			try {				
				albums = new JSONArray(json);
				
				if (albums != null) {
					for (int i = 0; i < albums.length(); i++) {
						JSONObject c = albums.getJSONObject(i);

						String id = c.getString(TAG_ID);
						String name = c.getString(TAG_NAME);

						HashMap<String, String> map = new HashMap<String, String>();

						map.put(TAG_ID, id);
						map.put(TAG_NAME, name);

						albumsList.add(map);
					}
				}else{
					Log.d("Albums: ", "null");
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
							CourtsActivity.this, albumsList,
							R.layout.list_courts, new String[] { TAG_ID,
									TAG_NAME }, new int[] {
									R.id.album_id, R.id.album_name });
					
					setListAdapter(adapter);
				}
			});

		}

	}
}