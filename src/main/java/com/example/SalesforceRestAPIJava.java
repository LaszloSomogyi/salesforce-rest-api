package com.example;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class SalesforceRestAPIJava {

    static final String USERNAME     = "lsomogyi@dev.interview.artemis-innovations.de";
    static final String PASSWORD     = "password";
    static final String LOGINURL     = "https://login.salesforce.com";
    static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
    static final String CLIENTID     = "id";
    static final String CLIENTSECRET = "cs";
    static final String REST_ENDPOINT = "/services/data" ;
    static final String API_VERSION = "/v32.0" ;
    private static String baseUri;
    private static Header oauthHeader;
    static final Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
    private static String accountId;

    public static void main(String[] args) {

        HttpClient httpclient = HttpClientBuilder.create().build();

        String loginURL = LOGINURL +
                GRANTSERVICE +
                "&client_id=" + CLIENTID +
                "&client_secret=" + CLIENTSECRET +
                "&username=" + USERNAME +
                "&password=" + PASSWORD;

        HttpPost httpPost = new HttpPost(loginURL);
        HttpResponse response = null;

        try {
            response = httpclient.execute(httpPost);
        } catch (IOException cpException) {
            cpException.printStackTrace();
        }

        assert response != null;
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            System.out.println("Error authenticating: "+statusCode);
            return;
        }

        String getResult = null;
        try {
            getResult = EntityUtils.toString(response.getEntity());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        JSONObject jsonObject = null;
        String loginAccessToken = null;
        String loginInstanceUrl = null;

        try {
            assert getResult != null;
            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            loginAccessToken = jsonObject.getString("access_token");
            loginInstanceUrl = jsonObject.getString("instance_url");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }

        baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION ;
        oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken) ;
        System.out.println("oauthHeader1: " + oauthHeader);
        System.out.println("\n" + response.getStatusLine());
        System.out.println("Successful login");
        System.out.println("instance URL: "+loginInstanceUrl);
        System.out.println("access token/session ID: "+loginAccessToken);
        System.out.println("baseUri: "+ baseUri);

        // Run function to query Accounts
        queryAccounts();

        // Release connection
        httpPost.releaseConnection();
    }


    // Function to query Accounts
    public static void queryAccounts() {
        System.out.println("\n_______________ QUERY RESULTS _______________");
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            String uri = baseUri + "/query?q=Select+Id+,+Name+,+Phone+From+Account+Limit+9";
            System.out.println("Query URL: " + uri);
            HttpGet httpGet = new HttpGet(uri);
            System.out.println("oauthHeader2: " + oauthHeader + '\n');
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            HttpResponse response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(response.getEntity());
                try {
                    JSONObject json = new JSONObject(response_string);
                    JSONArray jsonArray = json.getJSONArray("records");
                    for (int i = 0; i < jsonArray.length(); i++){
                        accountId = json.getJSONArray("records").getJSONObject(i).getString("Id");
                        String accountName = json.getJSONArray("records").getJSONObject(i).getString("Name");
                        String accountPhone = json.getJSONArray("records").getJSONObject(i).getString("Phone");
                        System.out.println("Account No. " + (i+1) + ":" + '\n' + "Id: " + accountId + ", Name: " + accountName + ", Phone: " + accountPhone);
                        System.out.println("Contacts connected to the Account:");
                        queryContacts(accountId);
                        System.out.println();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                System.out.println("Query was unsuccessful. Status code returned is " + statusCode);
                System.out.println("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
                System.out.println(getBody(response.getEntity().getContent()));
                System.exit(-1);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    // Function to query Contacts
    public static void queryContacts(String queriedAccountId) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            String contactUri = baseUri + "/query?q=Select+Id+,+FirstName+,+LastName+,AccountId+From+Contact+where+AccountId+='" + queriedAccountId + "'";

            HttpGet contactHttpGet = new HttpGet(contactUri);
            contactHttpGet.addHeader(oauthHeader);
            contactHttpGet.addHeader(prettyPrintHeader);

            HttpResponse contactResponse = httpClient.execute(contactHttpGet);

            int contactStatusCode = contactResponse.getStatusLine().getStatusCode();
            if (contactStatusCode == 200) {
                String contactResponse_string = EntityUtils.toString(contactResponse.getEntity());
                try {
                    JSONObject contactJson = new JSONObject(contactResponse_string);
                    JSONArray contactJsonArray = contactJson.getJSONArray("records");
                        for (int i = 0; i < contactJsonArray.length(); i++) {
                            String contactId = contactJson.getJSONArray("records").getJSONObject(i).getString("Id");
                            String contactFirstName = contactJson.getJSONArray("records").getJSONObject(i).getString("FirstName");
                            String contactLastName = contactJson.getJSONArray("records").getJSONObject(i).getString("LastName");
                            accountId = contactJson.getJSONArray("records").getJSONObject(i).getString("AccountId");
                            System.out.println((i+1) + ". Id: " + contactId + ", Name: " + contactFirstName + " " + contactLastName + ", (accountId): " + accountId);
                        }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                System.out.println("Query was unsuccessful. Status code returned is " + contactStatusCode);
                System.out.println("An error has occured. Http status: " + contactResponse.getStatusLine().getStatusCode());
                System.out.println(getBody(contactResponse.getEntity().getContent()));
                System.exit(-1);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    private static String getBody(InputStream inputStream) {
        String result = "";
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream)
            );
            String inputLine;
            while ( (inputLine = in.readLine() ) != null ) {
                result += inputLine;
                result += "\n";
            }
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

}

