/**
 * 
 */
package com.emuneee.nctrafficcams.api;

/**
 * Simplifies queries from the UI
 * @author ehalley
 *
 */
public class QueryType {
	
	public enum QueryMode {
		All,
		Favorites,
		City,
		Routes,
		NearMe
	};
	
	public QueryMode queryMode;
	public String query;
	
	public QueryType(QueryMode mode, String query) {
		this.queryMode = mode;
		this.query = query;
	}
	
	public QueryType(QueryMode mode) {
		this.queryMode = mode;
		this.query = null;
	}
}
