package kr.nkia.github;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static Gson gson;
	private static String GITHUB_API_BASE_URL = "https://api.github.com/";
	private static String GITHUB_API_SEARCH_CODE_PATH = "search/code?q=";
	private static String GITHUB_API_SEARCH_ISSUES_PATH = "search/issues";
	private static String GITHUB_API_SEARCH_COMMITS_PATH = "search/commits";
	
    public static void main( String[] args ) throws IOException, URISyntaxException
    {
        gson = new GsonBuilder().setPrettyPrinting().create();
        
        searchFileByFileName();
        searchCodeByContent();
        searchPullRequests();
        searchCommits();


//    	System.out.println( "Hello World!" );
    }
    
    private static void searchTree() throws ClientProtocolException, IOException {
        /*
		 * Call GitHub branches API REST end point & get JSON response. This response
		 * will also provide URL with treeSha for Tree REST endpoint.
		 */
//		Map jsonMap = makeRESTCall("https://api.github.com/repos/RaviKharatmal/test/branches/develop");
		Map jsonMap = makeRESTTreeCall("https://api.github.com/repos/limssung/202104405/branches/hotfix");
		System.out.println(
				"Branches API Response = \n<API RESPONSE START>\n " + gson.toJson(jsonMap) + "\n<API RESPONSE END>\n");
 
		/*
		 * Fetch Tree API REST endpoint URL from above response. We will use gson tree
		 * traversing methods to get this.
		 * 
		 * Path in JSON = root > commit > commit > tree > url
		 */
		String treeApiUrl = gson.toJsonTree(jsonMap).getAsJsonObject().get("commit").getAsJsonObject().get("commit")
				.getAsJsonObject().get("tree").getAsJsonObject().get("url").getAsString();
		System.out.println("TREE API URL = " + treeApiUrl + "\n");
 
		/*
		 * Now call GitHub Tree API to get tree of files with metadata. Added recursive
		 * parameter to get all files recursively.
		 */
		Map jsonTreeMap = makeRESTTreeCall(treeApiUrl + "?recursive=1");
		System.out.println(
				"TREE API Response = \n<API RESPONSE START>\n " + gson.toJson(jsonMap) + "\n<API RESPONSE END>\n");
 
		// Get tree list & iterate over it.
		System.out.println("Directory & files list :");
		for (Object obj : ((List) jsonTreeMap.get("tree"))) {
 
			Map fileMetadata = (Map) obj;
 
			// Type tree will be directory & blob will be file. Print files & directory
			// list with file sizes.
			if (fileMetadata.get("type").equals("tree")) {
				System.out.println("Directory = " + fileMetadata.get("path"));
			} else {
				System.out.println(
						"File = " + fileMetadata.get("path") + " | Size = " + fileMetadata.get("size") + " Bytes");
			}
		}
    }
    private static void searchCommits() throws ClientProtocolException, IOException {
    	 /*
    	 * Search commits
    	 * 
    	* ">" url encoded as "%3e" , "<" url encoded as "%3c"
    	 */
    	 String commitsQuery = "?q=author:garydgregory+committer-date:%3e2019-08-01";
    	 
    	 Map commitsSearchResult = makeRESTCall(GITHUB_API_BASE_URL + GITHUB_API_SEARCH_COMMITS_PATH + commitsQuery,
    	 "application/vnd.github.cloak-preview");
    	 
    	 System.out.println("Total number or results = searchCommits() ");
    	 System.out.println("Total number or results = " + commitsSearchResult.get("total_count"));
    	 gson.toJsonTree(commitsSearchResult).getAsJsonObject().get("items").getAsJsonArray()
    	 .forEach(r -> System.out
    	 .println("\n\t | Message: " + r.getAsJsonObject().get("commit").getAsJsonObject().get("message")
    	 + "\n\t | Date: " + r.getAsJsonObject().get("commit").getAsJsonObject().get("committer")
    	 .getAsJsonObject().get("date")));
    }
    
    private static void searchPullRequests() throws ClientProtocolException, IOException {
    	 /*
    	 * Search pull requests
    	 * 
    	* 1) Search in repo = "apache/commons-lang", 2) Type as Pull Requests 3) Only
    	 * open pull requests 4) Pull requests which are to be merged in master branch
    	 * 5) Sort by created date-time in ascending order.
    	 */
    	 String pullRequestsQuery = "?q=number+repo:apache/commons-lang+type:pr+state:open+base:master&sort=created&order=asc";
    	 
    	 Map pullRequestsSearchResult = makeRESTCall(
    	 GITHUB_API_BASE_URL + GITHUB_API_SEARCH_ISSUES_PATH + pullRequestsQuery);
    	 
    	 System.out.println("Total number or results = searchPullRequests() " );
    	 System.out.println("Total number or results = " + pullRequestsSearchResult.get("total_count"));
    	 gson.toJsonTree(pullRequestsSearchResult).getAsJsonObject().get("items").getAsJsonArray()
    	 .forEach(r -> System.out.println("\tTitle: " + r.getAsJsonObject().get("title") + "\n\t\t | By User: "
    	 + r.getAsJsonObject().get("user").getAsJsonObject().get("login") + "\n\t\t | Path: "
    	 + r.getAsJsonObject().get("pull_request").getAsJsonObject().get("html_url")));
    }
    
    private static void searchCodeByContent() throws ClientProtocolException, IOException {
    	 /*
    	 * Search Code by content in file
    	 * 
    	* 1) Search for word (method name) = "containsAny", 2) Search in files, 3)
    	 * Search in file with extension ".java", 4) Search in Repository =
    	 * https://github.com/apache/commons-lang
    	 */
    	 String codeContentQuery = "containsAny+in:file+language:java+repo:apache/commons-lang";
    	 
    	 Map contentSearchResult = makeRESTCall(GITHUB_API_BASE_URL + GITHUB_API_SEARCH_CODE_PATH + codeContentQuery,
    	 "application/vnd.github.v3.text-match+json");
    	 // System.out.println(
    	 // " Response = \n<API RESPONSE START>\n " + gson.toJson(contentSearchResult) +
    	 // "\n<API RESPONSE END>\n");
    	 System.out.println("Total number or results = searchCodeByContent() " );
    	 System.out.println("Total number or results = " + contentSearchResult.get("total_count"));
    	 gson.toJsonTree(contentSearchResult).getAsJsonObject().get("items").getAsJsonArray().forEach(r -> {
    	 System.out.println("\tFile: " + r.getAsJsonObject().get("name") + "\n\t\t | Repo: "
    	 + r.getAsJsonObject().get("repository").getAsJsonObject().get("html_url") + "\n\t\t | Path: "
    	 + r.getAsJsonObject().get("path"));
    	 
    	 r.getAsJsonObject().get("text_matches").getAsJsonArray()
    	 .forEach(t -> System.out.println("\t\t| Matched line: " + t.getAsJsonObject().get("fragment")));
    	 });
     }
    
    
    private static void searchFileByFileName() throws ClientProtocolException, IOException {
    	 /*
    	 * Search files by file name
    	 * 
    	* 1) Search for file name containing "WordUtil", 2) File extension = "java" 3)
    	 * File from any repo of organization "apache"
    	 */
    	 String codeFileQuery = "filename:WordUtil+extension:java+org:apache";
    	 
    	 Map fileNameSearchResult = makeRESTCall(GITHUB_API_BASE_URL + GITHUB_API_SEARCH_CODE_PATH + codeFileQuery);
    	 
    	 System.out.println("Total number or results = searchFileByFileName() " );
    	 System.out.println("Total number or results = " + fileNameSearchResult.get("total_count"));

    	 gson.toJsonTree(fileNameSearchResult).getAsJsonObject().get("items").getAsJsonArray()
    	 .forEach(r -> System.out.println("\tFile: " + r.getAsJsonObject().get("name") + "\n\t\t | Repo: "
    	 + r.getAsJsonObject().get("repository").getAsJsonObject().get("html_url") + "\n\t\t | Path: "
    	 + r.getAsJsonObject().get("path")));
    }
    
    private static Map makeRESTCall(String restUrl, String acceptHeaderValue)
    		 throws ClientProtocolException, IOException {
		 Request request = Request.Get(restUrl);
		 
		 if (acceptHeaderValue != null && !acceptHeaderValue.isEmpty()) {
		 request.addHeader("Accept", acceptHeaderValue);
		 }
		 
		 Content content = request.execute().returnContent();
		 String jsonString = content.asString();
		 // System.out.println("content = " + jsonString);
		 
		 // To print response JSON, using GSON. Any other JSON parser can be used here.
		 Map jsonMap = gson.fromJson(jsonString, Map.class);
		 return jsonMap;
	}

    private static Map makeRESTCall(String restUrl) throws ClientProtocolException, IOException {
    	 return makeRESTCall(restUrl, null);

    }
    
    private static Map makeRESTTreeCall(String restUrl) throws ClientProtocolException, IOException {

    	Content content = Request.Get(restUrl).execute().returnContent();
		String jsonString = content.asString();
		System.out.println("content = " + jsonString);

		// To print response JSON, using GSON. Any other JSON parser can be used here.
		Map jsonMap = gson.fromJson(jsonString, Map.class);
		return jsonMap;
   }
    
    private static void test() throws ClientProtocolException, IOException {
    }

}
