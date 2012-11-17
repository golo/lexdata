package com.udes.lex;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.udes.lex.helper.AlertDialogManager;
import com.udes.lex.helper.ConnectionDetector;
import com.udes.lex.helper.JSONParser;
import com.udes.lexdata.R;

public class SingleSentenceActivity extends Activity {

	ConnectionDetector cd;
	
	AlertDialogManager alert = new AlertDialogManager();
	
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();

	JSONArray albums = null;
	
	String court_id = null;
	String song_id = null;
	
	String ponent_name, name, date, issues;

	private static final String URL_SENTENCE = "http://blacaman.com/app/sentences/view/";

	private static final String TAG_NAME = "number";
	private static final String TAG_DATE = "date";
	private static final String TAG_PONENT = "ponent";
	private static final String TAG_ISSUE ="issues";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_sentence);
		
		cd = new ConnectionDetector(getApplicationContext());
		 
        if (!cd.isConnectingToInternet()) {
            alert.showAlertDialog(SingleSentenceActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            return;
        }
        
        Intent i = getIntent();
        court_id = i.getStringExtra("court_id");
        song_id = i.getStringExtra("song_id");
        
        new LoadSingleTrack().execute();
	}
	

	class LoadSingleTrack extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SingleSentenceActivity.this);
			pDialog.setMessage("Cargando sentencia...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) {

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("id", song_id));

			String json = jsonParser.makeHttpRequest(URL_SENTENCE, "GET",
					params);

			Log.d("Single Track JSON: ", json);

			try {
				JSONObject jObj = new JSONObject(json);
				if(jObj != null){
					name = jObj.getString(TAG_NAME);
					ponent_name = jObj.getString(TAG_PONENT);
					date = jObj.getString(TAG_DATE);
					issues = jObj.getString(TAG_ISSUE);
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
					
					TextView txt_song_name = (TextView) findViewById(R.id.song_title);
					TextView txt_album_name = (TextView) findViewById(R.id.album_name);
					TextView txt_duration = (TextView) findViewById(R.id.duration);
					TextView txt_issues = (TextView) findViewById(R.id.issues);
					
					txt_song_name.setText(name);
					txt_album_name.setText(Html.fromHtml("<b>Ponente:</b> " + ponent_name));
					txt_duration.setText(Html.fromHtml("<b>Fecha:</b> " + date));
					txt_issues.setText(Html.fromHtml("<b>Temas:</b> " + issues));
					
					setTitle(name);
				}
			});

		}

	}
}