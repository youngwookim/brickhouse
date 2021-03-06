package brickhouse.hbase;
/**
 * Copyright 2012 Klout, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.log4j.Logger;

/**
 *   Simple UDF for doing single PUT into HBase table ..
 *  Not intended for doing massive reads from HBase,
 *   but only when relatively few rows are being read.  
 *   
 */
@Description(name="hbase_put",
value = "_FUNC_(table,key,family) - Do a single HBase Put on a table " 
)
public class PutUDF extends UDF {
	private static final Logger LOG = Logger.getLogger( PutUDF.class);
	private static Map<String,HTable> htableMap = new HashMap<String,HTable>();
	private static Configuration config = new Configuration(true);
	
	static private byte[] FAMILY = "c".getBytes();
	static private byte[] QUALIFIER = "q".getBytes();
	

	
	public String evaluate( String tableName, String key, String value) {
		try {
	       HTable table = getHTable( tableName);
	       Put thePut = new Put( key.getBytes());
	       thePut.add( FAMILY, QUALIFIER, value.getBytes());
	       
	       table.put(thePut);
	       return "Put " + key + ":" + value;
		} catch(Exception exc ) {
			LOG.error( "Error while doing HBase Puts");
			
			 ///LOG.error(" Error while trying HBase PUT ",exc);
			 throw new RuntimeException(exc);
		}
		
		
	}
	
	private HTable getHTable(String tableName ) throws IOException {
	   HTable table = htableMap.get( tableName);
	   if(table == null) {
		   config = new Configuration(true);
		   Iterator<Entry<String,String>> iter = config.iterator();
		   while( iter.hasNext()  ) {
			  Entry<String,String> entry =  iter.next(); 
			  LOG.info(" BEFORE CONFIG = " + entry.getKey() + " == " + entry.getValue() );
		   }
		   config.set("hbase.zookeeper.quorum", "jobs-dev-zoo1,jobs-dev-zoo2,jobs-dev-zoo3");
		   Configuration hbConfig = HBaseConfiguration.create( config);
		    iter = hbConfig.iterator();
		   while( iter.hasNext()  ) {
			  Entry<String,String> entry =  iter.next(); 
			  LOG.info(" AFTER CONFIG = " + entry.getKey() + " == " + entry.getValue() );
		   }
		  table =   new HTable( hbConfig, tableName);
		  htableMap.put( tableName, table);
	   }
	
	   return table;
	}
}
