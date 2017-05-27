package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Switch;

import static android.content.ContentValues.TAG;

public class SimpleDynamoProvider extends ContentProvider {

	LinkedList<String> l1 = new LinkedList<String>();
	LinkedList<String> listforHash = new LinkedList<String>();
	Dictionary<String, String> dict = new Hashtable<String, String>();
	String myport="";
	String succ1 ="";
	String succ2 ="";
	public static String KEY_FIELD = "key";
	public static String VALUE_FIELD = "value";

	private Uri buildUri(String scheme, String authority) {

		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}



	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		try {
			if(selection.contains("#")) {
				String deletestr []=selection.split("//");
				String todelete = deletestr[0];
				Log.d("valuecametodelete",deletestr[0]+"::"+deletestr[1]);
				SharedPreferences msg = PreferenceManager.getDefaultSharedPreferences(getContext());
				SharedPreferences.Editor editor = msg.edit();
				editor.remove(todelete);
				editor.commit();
				Log.d("valuefinallydelete","done");
			}
			else {
				String hashkey = genHash(selection);
				int j = 0;
				if (hashkey.compareTo(listforHash.get(j)) > 0 && hashkey.compareTo(listforHash.get(j + 1)) < 0) {
					myport = dict.get(listforHash.get(j + 1));
					succ1 = dict.get(listforHash.get(j + 2));
					succ2 = dict.get(listforHash.get(j + 3));
				} else if (hashkey.compareTo(listforHash.get(j + 1)) > 0 && hashkey.compareTo(listforHash.get(j + 2)) < 0) {
					myport = dict.get(listforHash.get(j + 2));
					succ1 = dict.get(listforHash.get(j + 3));
					succ2 = dict.get(listforHash.get(j + 4));
				} else if (hashkey.compareTo(listforHash.get(j + 2)) > 0 && hashkey.compareTo(listforHash.get(j + 3)) < 0) {
					myport = dict.get(listforHash.get(j + 3));
					succ1 = dict.get(listforHash.get(j + 4));
					succ2 = dict.get(listforHash.get(j));
				} else if (hashkey.compareTo(listforHash.get(j + 3)) > 0 && hashkey.compareTo(listforHash.get(j + 4)) < 0) {
					myport = dict.get(listforHash.get(j + 4));
					succ1 = dict.get(listforHash.get(j));
					succ2 = dict.get(listforHash.get(j + 1));
				} else if (hashkey.compareTo(listforHash.get(j + 4)) > 0 || hashkey.compareTo(listforHash.get(j)) < 0) {
					myport = dict.get(listforHash.get(j));
					succ1 = dict.get(listforHash.get(j + 1));
					succ2 = dict.get(listforHash.get(j + 2));
				}

				String[] myarr = {myport, succ1, succ2};

					Socket soc = null;
					for (int k = 0; k < myarr.length; k++) {
						try {
						soc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(String.valueOf(Integer.valueOf(myarr[k]) * 2)));
							soc.setSoTimeout(1000);
						String msgToSend = "DELETE" + "//" + selection;
						DataOutputStream dd = new DataOutputStream(soc.getOutputStream());
						dd.writeUTF(msgToSend);
						Log.d("DELETE", msgToSend);
					}
						catch (SocketException  e){
							e.printStackTrace();
							continue;
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

			try {
				SharedPreferences msg = PreferenceManager.getDefaultSharedPreferences(getContext());
				SharedPreferences.Editor editor = msg.edit();

				String foraddinInsert = String.valueOf(values.get(VALUE_FIELD));

				if(foraddinInsert.contains("#")){
					String valuetoInsert[]=foraddinInsert.split("//");
					Log.d("controlcame1",valuetoInsert[0]+"::"+valuetoInsert[1]);
				    values.put(KEY_FIELD,String.valueOf(values.get(KEY_FIELD)));
					values.put(VALUE_FIELD,valuetoInsert[0]);

					editor.putString(values.getAsString(KEY_FIELD),values.getAsString(VALUE_FIELD));
					editor.commit();
					Log.d("INSERT", "INDB"+"::"+String.valueOf(values.get(KEY_FIELD))+"::"+valuetoInsert[0]);
					return uri;
				}
				else {
					String hashkey = genHash(String.valueOf((values.get(KEY_FIELD))));
					Log.d("hashkey", hashkey + "::" + values.get(KEY_FIELD));

					int j = 0;
					Log.d("listforHash", listforHash.get(j) + "::" + listforHash.get(j + 1) + "::" + listforHash.get(j + 2) + "::" + listforHash.get(j + 3) + "::" + listforHash.get(j + 4));
					Log.d("listforHash", dict.get(listforHash.get(j)) + "::" + dict.get(listforHash.get(j + 1)) + "::" + dict.get(listforHash.get(j + 2)) + "::" + dict.get(listforHash.get(j + 3)) + "::" + dict.get(listforHash.get(j + 4)));

					if (hashkey.compareTo(listforHash.get(j)) > 0 && hashkey.compareTo(listforHash.get(j + 1)) < 0) {
						myport = dict.get(listforHash.get(j + 1));
						succ1 = dict.get(listforHash.get(j + 2));
						succ2 = dict.get(listforHash.get(j + 3));
					} else if (hashkey.compareTo(listforHash.get(j + 1)) > 0 && hashkey.compareTo(listforHash.get(j + 2)) < 0) {
						myport = dict.get(listforHash.get(j + 2));
						succ1 = dict.get(listforHash.get(j + 3));
						succ2 = dict.get(listforHash.get(j + 4));
					} else if (hashkey.compareTo(listforHash.get(j + 2)) > 0 && hashkey.compareTo(listforHash.get(j + 3)) < 0) {
						myport = dict.get(listforHash.get(j + 3));
						succ1 = dict.get(listforHash.get(j + 4));
						succ2 = dict.get(listforHash.get(j));
					} else if (hashkey.compareTo(listforHash.get(j + 3)) > 0 && hashkey.compareTo(listforHash.get(j + 4)) < 0) {
						myport = dict.get(listforHash.get(j + 4));
						succ1 = dict.get(listforHash.get(j));
						succ2 = dict.get(listforHash.get(j + 1));
					} else if (hashkey.compareTo(listforHash.get(j + 4)) > 0 || hashkey.compareTo(listforHash.get(j)) < 0) {
						myport = dict.get(listforHash.get(j));
						succ1 = dict.get(listforHash.get(j + 1));
						succ2 = dict.get(listforHash.get(j + 2));
					}
					Socket soc = null;


					String[] myarr = {myport, succ1, succ2};
					Log.d("finaltosend1", myport + "::" + succ1 + "::" + succ2);


					for (int k = 0; k < myarr.length; k++) {
						try {
							soc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(myarr[k]) * 2);
							String msgToSend = "INSERT" + "//" + values.get(KEY_FIELD) + "//" + values.get(VALUE_FIELD);
							DataOutputStream dd = new DataOutputStream(soc.getOutputStream());
							dd.writeUTF(msgToSend);
							Log.d("REQUEST1", myarr[k]);

						} catch (SocketException e) {
							e.printStackTrace();
						}
					}


				}


			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			/*try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			return uri;
		}

	@Override
	public boolean onCreate() {
		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		Global.node_id = String.valueOf(Integer.valueOf(myPort) / 2);

			String[] REMOTE_PORT = {"5554", "5556", "5558", "5560", "5562"};
			for (int i = 0; i < REMOTE_PORT.length; i++) {
				String hash_values = null;
				try {
					hash_values = genHash(REMOTE_PORT[i]);
				} catch (Exception e1) {

				}
				dict.put(hash_values, REMOTE_PORT[i]);
				listforHash.add(hash_values);
			}

			Collections.sort(listforHash);
			ListIterator iter = listforHash.listIterator();
			l1.clear();
			while (iter.hasNext()) {
				String x = String.valueOf(iter.next());
				l1.add(dict.get(x));
			}
			Log.d("mylist",l1.get(0)+"::"+l1.get(1)+"::"+l1.get(2)+"::"+l1.get(3)+"::"+l1.get(4));

//		new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Global.node_id, myPort);
		new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return false;

	}
	// TODO Auto-generated method stub




	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		try {
			Cursor c = null;
			String[] projection1 = {
					KEY_FIELD, VALUE_FIELD
			};


			 if (selection.contains("@")) {
				 MatrixCursor mc = new MatrixCursor(new String[]{KEY_FIELD,VALUE_FIELD});

				 Log.d("QUERY@","letsc");

				 SharedPreferences msg = PreferenceManager.getDefaultSharedPreferences(getContext());
				 Map<String,?> mapp = msg.getAll();
				 for (Map.Entry<String,?> entr : mapp.entrySet()){
					 String newstr[]= {entr.getKey(),entr.getValue().toString()};
					 Log.d("valuesrecieveat@",newstr[0]+"::"+newstr[0]);
					 mc.addRow(newstr);
				 }

				return mc;

			}
			else if(selection.contains("*")){
				/*if(selection.contains("&")) {
					Log.d("QUERY*","entered");
					Cursor newcur[]=new Cursor[5];
					for(int s=0;s<newcur.length;s++) {
						c = db.query(MESSAGES_TABLE_NAME, projection1, null,
								null, null, null, null);
						newcur[s]=c;
					}
                    Log.d("mergecursor","combining");
					MergeCursor merge_cursor = new MergeCursor(new Cursor[] {
							newcur[0],newcur[1],newcur[2],newcur[3],newcur[4]});
					return  merge_cursor;

				}
				else{*/

					Socket querySocket = null;
				MatrixCursor mc = new MatrixCursor(new String[]{KEY_FIELD,VALUE_FIELD});
					for (int k = 0; k < l1.size(); k++) {
						try{
						querySocket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(String.valueOf(Integer.valueOf(l1.get(k)) * 2)));
						String msgToSend = "QUERY" + "//" + "@";
						DataOutputStream dd = new DataOutputStream(querySocket.getOutputStream());
						dd.writeUTF(msgToSend);
						Log.d("QUERY*", msgToSend);

						DataInputStream inmove = new DataInputStream(querySocket.getInputStream());
						String datarecive=null;
						datarecive = inmove.readUTF();
						Log.d("QUERYcome*", datarecive);
						String vvv[]=datarecive.split("//");
						Log.d("function was split",vvv.toString());

						for(int f=0;f<vvv.length;f++){
							String dbh =vvv[f];
							Log.d("thearraynew",dbh);
							String jbd[]=dbh.split(" ");
							mc.addRow(jbd);
							Log.d("rowtest",mc.toString());
						}

					}catch (Exception e){
							e.printStackTrace();
							continue;
						}
					}
				    return mc;
			//	}

			}
			else{
				if(selection.contains("&")) {
					MatrixCursor mc = new MatrixCursor(new String[]{KEY_FIELD,VALUE_FIELD});
					Log.d("CAMETOQUERY",selection);
					String selection1 = KEY_FIELD + " = ?";
					String newSelection[]=selection.split("//");
					String selectdone = newSelection[0];

					SharedPreferences msg = PreferenceManager.getDefaultSharedPreferences(getContext());

					String val = msg.getString(selectdone,null);
					String arrstr [] = {selectdone,val};
					mc.addRow(arrstr);

					Log.d("query1",selectdone);

					return mc;


				}
				else {
					//	String selection1 = KEY_FIELD + " = ?";
					//	String[] selectionArgs1 = {selection};
					String hashkey = genHash(selection);
					int j = 0;
					if (hashkey.compareTo(listforHash.get(j)) > 0 && hashkey.compareTo(listforHash.get(j + 1)) < 0) {
						myport = dict.get(listforHash.get(j + 1));
						succ1 = dict.get(listforHash.get(j + 2));
						succ2 = dict.get(listforHash.get(j + 3));
					} else if (hashkey.compareTo(listforHash.get(j + 1)) > 0 && hashkey.compareTo(listforHash.get(j + 2)) < 0) {
						myport = dict.get(listforHash.get(j + 2));
						succ1 = dict.get(listforHash.get(j + 3));
						succ2 = dict.get(listforHash.get(j + 4));
					} else if (hashkey.compareTo(listforHash.get(j + 2)) > 0 && hashkey.compareTo(listforHash.get(j + 3)) < 0) {
						myport = dict.get(listforHash.get(j + 3));
						succ1 = dict.get(listforHash.get(j + 4));
						succ2 = dict.get(listforHash.get(j + 0));
					} else if (hashkey.compareTo(listforHash.get(j + 3)) > 0 && hashkey.compareTo(listforHash.get(j + 4)) < 0) {
						myport = dict.get(listforHash.get(j + 4));
						succ1 = dict.get(listforHash.get(j));
						succ2 = dict.get(listforHash.get(j + 1));
					} else if (hashkey.compareTo(listforHash.get(j + 4)) > 0 || hashkey.compareTo(listforHash.get(j)) < 0) {
						myport = dict.get(listforHash.get(j));
						succ1 = dict.get(listforHash.get(j + 1));
						succ2 = dict.get(listforHash.get(j + 2));
					}
					Log.d("MYPORT", myport+"::"+succ1+"::"+succ2);
					String myarr[] = {myport, succ1, succ2};
					Socket soc = null;
					MatrixCursor mcnew = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
					MatrixCursor mc = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});

					for (int k = 0; k < myarr.length; k++) {
						try{
						soc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(myarr[k]) * 2);
						String msgToSend = "QUERY" + "//" + selection;
						DataOutputStream dd = new DataOutputStream(soc.getOutputStream());
						dd.writeUTF(msgToSend);
						Log.d("QUERY1", msgToSend);

						/*DataInputStream inmove = new DataInputStream(soc.getInputStream());
						String datarecive = null;
						datarecive = inmove.readUTF();
						String jdj[] = datarecive.split("//");

						String hhh[] = jdj[0].split(" ");
						Log.d("split1", datarecive);
						MatrixCursor mc = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
						mc.addRow(hhh);*/

						DataInputStream inmove = new DataInputStream(soc.getInputStream());
						String datarecive=null;
						datarecive = inmove.readUTF();
							if(datarecive==null){
								continue;
							}

						Log.d("QUERYcome*", datarecive);
						String vvv[]=datarecive.split("//");
						Log.d("function was split",vvv.toString());

							for (int f = 0; f < vvv.length; f++) {
								String dbh = vvv[f];
								Log.d("thearraynew", dbh);
								String jbd[] = dbh.split(" ");
								mc.addRow(jbd);
								Log.d("rowtest", mc.toString());
							}
							return mc;

					}catch (Exception e){
							e.printStackTrace();
							continue;
						}
					}
	/*				mc.moveToFirst();
					int keyIndex1 = mc.getColumnIndex(KEY_FIELD);
					int valueIndex1 = mc.getColumnIndex(VALUE_FIELD);
					String array1 = mc.getString(keyIndex1);
					String arr21 = mc.getString(valueIndex1);
					if(array1==null) {
						mc.moveToNext();
						int keyIndex = mc.getColumnIndex(KEY_FIELD);
						int valueIndex = mc.getColumnIndex(VALUE_FIELD);
						String array = mc.getString(keyIndex);
						String arr2 = mc.getString(valueIndex);
						if(array==null){
							mc.moveToNext();
							int keyIndex2 = mc.getColumnIndex(KEY_FIELD);
							int valueIndex2 = mc.getColumnIndex(VALUE_FIELD);
							String array2 = mc.getString(keyIndex2);
							String arr22 = mc.getString(valueIndex2);
							String finaltoadd[] = {array2, arr22};
							mcnew.addRow(finaltoadd);
						}
						else {
							String finaltoadd[] = {array, arr2};
							mcnew.addRow(finaltoadd);
						}
					}
					else {
						Log.d("Stringfinalnext", array1 + "::" + arr21);
						String finaltoadd[] = {array1,arr21};
						mcnew.addRow(finaltoadd);
					}
*/
					/*if(array==null){
						mc.moveToNext();



						if(array1==null ){
							mc.moveToNext();
							int keyIndex2 = mc.getColumnIndex(KEY_FIELD);
							int valueIndex2 = mc.getColumnIndex(VALUE_FIELD);
							String array11 = mc.getString(keyIndex2);
							String arr211 = mc.getString(valueIndex2);
							Log.d("Stringfinalnext", array11 + "::" + arr211);
							String finaltoadd1[] = {array11, arr211};
							mcnew.addRow(finaltoadd1);
						}
						else {
							String finaltoadd[] = {array1, arr21};
							mcnew.addRow(finaltoadd);
						}
					}
					else {
						Log.d("Stringfinalstart", array + "::" + arr2);
						String finaltoadd[] = {array,arr2};
						mcnew.addRow(finaltoadd);
					}*/

				}
			}
		}catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} /*catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
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



	private class ServerTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... msgs) {
			try {
				Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");


				String deadport = Global.node_id;
				Log.d("myportdead", deadport);
				String recoverRequest = "";
				String recoverRequest2 = "";

				if (l1.get(0).equals(deadport)) {
					recoverRequest = l1.get(1);
					recoverRequest2 = l1.get(3);
				} else if (l1.get(1).equals(deadport)) {
					recoverRequest = l1.get(2);
					recoverRequest2 = l1.get(4);
				} else if (l1.get(2).equals(deadport)) {
					recoverRequest = l1.get(3);
					recoverRequest2 = l1.get(0);
				} else if (l1.get(3).equals(deadport)) {
					recoverRequest = l1.get(4);
					recoverRequest2 = l1.get(1);
				} else if (l1.get(4).equals(deadport)) {
					recoverRequest = l1.get(0);
					recoverRequest2 = l1.get(2);
				}
				String[] myarr1 = {recoverRequest, recoverRequest2};
				Log.d("recoversend", deadport + "::" + recoverRequest + "::" + recoverRequest2);
				Socket soc = null;
				for (int k = 0; k < myarr1.length; k++) {
					try {
						soc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(myarr1[k]) * 2);
						String msgToSend = "IAMUP" +"//"+ "@";
						DataOutputStream dd = null;
						dd = new DataOutputStream(soc.getOutputStream());
						dd.writeUTF(msgToSend);


						DataInputStream inmove = null;
						inmove = new DataInputStream(soc.getInputStream());


						String datarecive = null;
						datarecive = inmove.readUTF();
						Log.d("QUERYcome*", datarecive);
						String vvv[] = datarecive.split("//");
						Log.d("function was split", vvv.toString());
						String hashdeadport = genHash(deadport);

						for (int f = 0; f < vvv.length; f++) {
							String dbh = vvv[f];
							Log.d("thearraynew", dbh);
							String jbd[] = dbh.split(" ");
							if (jbd.length > 1) {

								String hashport1 = "";
								String hashport2 = "";
								String hashport3 = "";

								String hashnewkeyadd = genHash(jbd[0]);

								Log.d("entering ifelse", hashnewkeyadd);

								switch (Integer.parseInt(deadport)) {
									case 5562:
										hashport1 = listforHash.get(4);
										hashport2 = listforHash.get(3);
										hashport3 = listforHash.get(2);
										Log.d("The decided ports are", jbd[0] + "::" + deadport + "::" + l1.get(4) + "::" + l1.get(3) + "::" + l1.get(2));
										if ((hashnewkeyadd.compareTo(hashport1) > 0 || hashnewkeyadd.compareTo(hashdeadport) < 0) || (hashnewkeyadd.compareTo(hashport2) > 0 && hashnewkeyadd.compareTo(hashport1) < 0) || (hashnewkeyadd.compareTo(hashport3) > 0 && hashnewkeyadd.compareTo(hashport2) < 0)) {
											Log.d("enterif", jbd[0] + "::" + hashnewkeyadd);
											ContentValues cv = new ContentValues();
											cv.put(KEY_FIELD, jbd[0]);
											cv.put(VALUE_FIELD, jbd[1] + "//" + "#");
											Log.d("COMMONCODE", jbd[0] + " " + jbd[1]);
											insert(mUri, cv);
											Log.d("value inserted", "endif");

										}
										break;
									case 5556:
									/*hashport1 = genHash(l1.get(0));
									hashport2 = genHash(l1.get(4));
									hashport3 = genHash(l1.get(3));*/
										hashport1 = listforHash.get(0);
										hashport2 = listforHash.get(4);
										hashport3 = listforHash.get(3);
										Log.d("The decided ports are", jbd[0] + "::" + deadport + "::" + l1.get(0) + "::" + l1.get(4) + "::" + l1.get(3));
										if ((hashnewkeyadd.compareTo(hashport1) > 0 && hashnewkeyadd.compareTo(hashdeadport) < 0) || (hashnewkeyadd.compareTo(hashport2) > 0 || hashnewkeyadd.compareTo(hashport1) < 0) || (hashnewkeyadd.compareTo(hashport3) > 0 && hashnewkeyadd.compareTo(hashport2) < 0)) {
											Log.d("enterif", jbd[0] + "::" + hashnewkeyadd);
											ContentValues cv = new ContentValues();
											cv.put(KEY_FIELD, jbd[0]);
											cv.put(VALUE_FIELD, jbd[1] + "//" + "#");
											Log.d("COMMONCODE", jbd[0] + " " + jbd[1]);
											insert(mUri, cv);
											Log.d("value inserted", "endif");

										}
										break;
									case 5554:
									/*hashport1 = genHash(l1.get(1));
									hashport2 = genHash(l1.get(0));
									hashport3 = genHash(l1.get(4));*/
										hashport1 = listforHash.get(1);
										hashport2 = listforHash.get(0);
										hashport3 = listforHash.get(4);
										Log.d("The decided ports are", jbd[0] + "::" + deadport + "::" + l1.get(1) + "::" + l1.get(0) + "::" + l1.get(4));
										if ((hashnewkeyadd.compareTo(hashport1) > 0 && hashnewkeyadd.compareTo(hashdeadport) < 0) || (hashnewkeyadd.compareTo(hashport2) > 0 && hashnewkeyadd.compareTo(hashport1) < 0) || (hashnewkeyadd.compareTo(hashport3) > 0 || hashnewkeyadd.compareTo(hashport2) < 0)) {
											Log.d("enterif", jbd[0] + "::" + hashnewkeyadd);
											ContentValues cv = new ContentValues();

											cv.put(KEY_FIELD, jbd[0]);
											cv.put(VALUE_FIELD, jbd[1] + "//" + "#");
											Log.d("COMMONCODE", jbd[0] + " " + jbd[1]);
											insert(mUri, cv);
											Log.d("value inserted", "endif");

										}
										break;
									case 5558:
									/*hashport1 = genHash(l1.get(2));
									hashport2 = genHash(l1.get(1));
									hashport3 = genHash(l1.get(0));*/
										hashport1 = listforHash.get(2);
										hashport2 = listforHash.get(1);
										hashport3 = listforHash.get(0);
										Log.d("The decided ports are", jbd[0] + "::" + deadport + "::" + l1.get(2) + "::" + l1.get(1) + "::" + l1.get(0));
										if ((hashnewkeyadd.compareTo(hashport1) > 0 && hashnewkeyadd.compareTo(hashdeadport) < 0) || (hashnewkeyadd.compareTo(hashport2) > 0 && hashnewkeyadd.compareTo(hashport1) < 0) || (hashnewkeyadd.compareTo(hashport3) > 0 && hashnewkeyadd.compareTo(hashport2) < 0)) {
											Log.d("enterif", jbd[0] + "::" + hashnewkeyadd);
											ContentValues cv = new ContentValues();
											cv.put(KEY_FIELD, jbd[0]);
											cv.put(VALUE_FIELD, jbd[1] + "//" + "#");
											Log.d("COMMONCODE", jbd[0] + " " + jbd[1]);
											insert(mUri, cv);
											Log.d("value inserted", "endif");

										}
										break;
									case 5560:
									/*hashdeadport = genHash(deadport);
									hashport1 = genHash(l1.get(3));
									hashport2 = genHash(l1.get(2));
									hashport3 = genHash(l1.get(1));*/
										hashport1 = listforHash.get(3);
										hashport2 = listforHash.get(2);
										hashport3 = listforHash.get(1);
										Log.d("The decided ports are", jbd[0] + "::" + deadport + "::" + l1.get(3) + "::" + l1.get(2) + "::" + l1.get(1));
										if ((hashnewkeyadd.compareTo(hashport1) > 0 && hashnewkeyadd.compareTo(hashdeadport) < 0) || (hashnewkeyadd.compareTo(hashport2) > 0 && hashnewkeyadd.compareTo(hashport1) < 0) || (hashnewkeyadd.compareTo(hashport3) > 0 && hashnewkeyadd.compareTo(hashport2) < 0)) {
											Log.d("enterif", jbd[0] + "::" + hashnewkeyadd);
											ContentValues cv = new ContentValues();
											cv.put(KEY_FIELD, jbd[0]);
											cv.put(VALUE_FIELD, jbd[1] + "//" + "#");
											Log.d("COMMONCODE", jbd[0] + " " + jbd[1]);
											insert(mUri, cv);
											Log.d("value inserted", "endif");

										}
										break;

								}


							}
						}
					} catch (SocketException sokc) {
						sokc.printStackTrace();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}

			}
			catch (NoSuchAlgorithmException NSE)
			{
				NSE.printStackTrace();
			}
			return null;
		}
	}
	}
