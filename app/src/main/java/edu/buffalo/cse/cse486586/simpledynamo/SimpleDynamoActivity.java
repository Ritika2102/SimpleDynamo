package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import static android.content.ContentValues.TAG;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.KEY_FIELD;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.VALUE_FIELD;

public class SimpleDynamoActivity extends Activity {
	static final String REMOTE_PORT0 = "11108";
	static final String REMOTE_PORT1 = "11112";
	static final String REMOTE_PORT2 = "11116";
	static final String REMOTE_PORT3 = "11120";
	static final String REMOTE_PORT4 = "11124";
	static final int SERVER_PORT = 10000;



	private Uri buildUri(String scheme, String authority) {

		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	public String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dynamo);

		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		 Global.node_id = String.valueOf(Integer.valueOf(myPort) / 2);



		try {
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {
			Log.e(TAG, "Can't create a ServerSocket");
			return;
		}

//		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, String.valueOf(node_id), myPort);


		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setMovementMethod(new ScrollingMovementMethod());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simple_dynamo, menu);
		return true;
	}

	public void onStop() {
		super.onStop();
		Log.v("Test", "onStop()");
	}

	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
		// int initial =0;

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			try {
//				Socket newsocket = serverSocket.accept();
				while(true) {
					Socket newsocket = serverSocket.accept();
					DataInputStream br = new DataInputStream(newsocket.getInputStream());
					String str = null;
					str = br.readUTF();
					Log.d("MSG IS READ", str);
					String msgToadd[] = str.split("//");
					String s1 = msgToadd[0]; //DELETE // INSERT // QUERY // IAMUP
					Log.d("DECIDE", s1);

					if (s1.equals("INSERT")) {
						Log.d("activity insert", "came");
						String key_recieve = msgToadd[1];
						String value_recieve = msgToadd[2]+"//"+"#";

						Log.d("valuesinsert", key_recieve + "//" + value_recieve);
						ContentValues cv = new ContentValues();
						cv.put(KEY_FIELD, key_recieve);
						cv.put(VALUE_FIELD, value_recieve);

						getContentResolver().insert(mUri, cv);


					} else if (s1.equals("QUERY")) {

						String selection_receive = msgToadd[1];
						if(selection_receive.contains("@")){
							Log.d("activity query",selection_receive);
							Cursor receive= getContentResolver().query(mUri,null,selection_receive,null,null);
							int keyIndex = receive.getColumnIndex(KEY_FIELD);
							int valueIndex = receive.getColumnIndex(VALUE_FIELD);
							String data = "";
							while (receive.moveToNext()) {
								data = data + (receive.getString(keyIndex) + " " + receive.getString(valueIndex)) + "//";
							}
							if (data == "")
								data = "Nothing";
							Log.d("testdata", data);

							DataOutputStream newdd = new DataOutputStream(newsocket.getOutputStream());
							newdd.writeUTF(data);
						}
                        else {
							String newsendingSelection = selection_receive + "//" + "&";
							Log.d("activity query", selection_receive + "::" + newsendingSelection);
							Cursor receive = getContentResolver().query(mUri, null, newsendingSelection, null, null);

							int keyIndex = receive.getColumnIndex(KEY_FIELD);
							int valueIndex = receive.getColumnIndex(VALUE_FIELD);
							String data = "";
							while (receive.moveToNext()) {
								data = data + (receive.getString(keyIndex) + " " + receive.getString(valueIndex)) + "//";
							}
							if (data == "")
								data = "Nothing";
							Log.d("testdata", data);

							DataOutputStream newdd = new DataOutputStream(newsocket.getOutputStream());
							newdd.writeUTF(data);

						}

					} else if (s1.equals("DELETE")) {
						String key_recieve = msgToadd[1];
						Log.d("delete",key_recieve);
						String newkey = key_recieve+"//"+"#";
						getContentResolver().delete(mUri, newkey, null);

					}
					else if (s1.equals("IAMUP")) {
						String key_recieve = msgToadd[1];
						Cursor receive= getContentResolver().query(mUri,null,key_recieve,null,null);

						int keyIndex = receive.getColumnIndex(KEY_FIELD);
						int valueIndex = receive.getColumnIndex(VALUE_FIELD);
						String data = "";
						while (receive.moveToNext()) {
							data = data + (receive.getString(keyIndex) + " " + receive.getString(valueIndex)) + "//";
						}
						if (data == "")
							data = "Nothing";
						Log.d("testdatarecover", data);

						DataOutputStream newdd = new DataOutputStream(newsocket.getOutputStream());
						newdd.writeUTF(data);


					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}


	protected void onProgressUpdate(String... strings) {

		String strReceived = strings[0].trim();
		TextView remoteTextView = (TextView) findViewById(R.id.textView1);
		remoteTextView.append(strReceived + "\t\n");

		return;
	}


	private class ClientTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... msgs) {

			/*String[] remotePort = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};
			Socket socket[] = new Socket[5];
			for (int i = 0; i < remotePort.length; i++) {
				try {
					socket[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(remotePort[i]));




				}catch(Exception out){

				}

			}*/
			return null;
		}

	}
}