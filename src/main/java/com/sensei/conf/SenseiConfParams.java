package com.sensei.conf;

import java.util.Comparator;

public interface SenseiConfParams {
	public static final String NODE_ID = "sensei.node.id";
	public static final String PARTITIONS = "sensei.node.partitions";

	public static final String SERVER_PORT = "sensei.server.port";
	public static final String SERVER_REQ_THREAD_POOL_SIZE = "sensei.server.requestThreadCorePoolSize";
	public static final String SERVER_REQ_THREAD_POOL_MAXSIZE = "sensei.server.requestThreadMaxPoolSize";
	public static final String SERVER_REQ_THREAD_POOL_KEEPALIVE = "sensei.server.requestThreadKeepAliveTimeSecs";
	
	public static final String SENSEI_CLUSTER_NAME = "sensei.cluster.name";
	public static final String SENSEI_CLUSTER_URL = "sensei.cluster.url";
	public static final String SENSEI_CLUSTER_TIMEOUT = "sensei.cluster.timeout";

	public static final String SENSEI_INDEX_DIR = "sensei.index.directory";

	public static final String SENSEI_INDEX_BATCH_SIZE = "sensei.index.batchSize";
	public static final String SENSEI_INDEX_BATCH_DELAY = "sensei.index.batchDelay";
	public static final String SENSEI_INDEX_BATCH_MAXSIZE = "sensei.index.maxBatchSize";
	public static final String SENSEI_INDEX_REALTIME = "sensei.index.realtime";
	public static final String SENSEI_INDEX_FRESHNESS = "sensei.index.freshness";
	

	public static final String SENSEI_INDEXER_TYPE = "sensei.indexer.type";
	public static final String SENSEI_INDEXER_TYPE_HOURGLASS = "hourglass";
	public static final String SENSEI_INDEXER_TYPE_ZOIE = "zoie";

	public static final String SENSEI_INDEX_ANALYZER = "sensei.index.analyzer";
	public static final String SENSEI_INDEX_SIMILARITY = "sensei.index.similarity";
	public static final String SENSEI_INDEX_INTERPRETER = "sensei.index.interpreter";
    public static final String SENSEI_INDEX_CUSTOM = "sensei.index.custom";
	public static final String SENSEI_QUERY_BUILDER_FACTORY = "sensei.query.builder.factory";
	public static final String SENSEI_INDEX_MANAGER = "sensei.index.manager";
	public static final String SENSEI_INDEX_MANAGER_FILTER = "sensei.index.manager.default.filter";

	public static final String SENSEI_VERSION_COMPARATOR = "sensei.version.comparator";

	public static final String SENSEI_PLUGIN_SVCS = "sensei.plugin.services";
	
	public static final String SENSEI_HOURGLASS_SCHEDULE = "sensei.indexer.hourglass.schedule";
	public static final String SENSEI_HOURGLASS_TRIMTHRESHOLD = "sensei.indexer.hourglass.trimthreshold";
	public static final String SENSEI_HOURGLASS_FREQUENCY = "sensei.indexer.hourglass.frequency";
	public static final String SENSEI_HOURGLASS_FREQUENCY_MIN = "minute";
	public static final String SENSEI_HOURGLASS_FREQUENCY_HOUR = "hour";
	public static final String SENSEI_HOURGLASS_FREQUENCY_DAY = "day";
	

	public static final String SERVER_BROKER_PORT = "sensei.broker.port";
	public static final String SERVER_BROKER_WEBAPP_PATH = "sensei.broker.webapp.path";

	public static final String SERVER_BROKER_MINTHREAD = "sensei.broker.minThread";
	public static final String SERVER_BROKER_MAXTHREAD = "sensei.broker.maxThread";
	public static final String SERVER_BROKER_MAXWAIT = "sensei.broker.maxWaittime";
	
	public static final String SERVER_SEARCH_ROUTER_FACTORY = "sensei.search.router.factory";
	
	public static final String SENSEI_INDEX_PRUNER = "sensei.index.pruner";

	public static final String SENSEI_ZOIE_RETENTION_DAYS = "sensei.index.zoie.retentionDays";

	public static final String SENSEI_MX4J_PORT = "sensei.mx4j.port";
	
	public static final Comparator<String> DEFAULT_VERSION_STRING_COMPARATOR = new Comparator<String>(){
		@Override
	    public int compare(String o1, String o2) {
		  if (o1==null && o2==null){
			return 0;
		  }
		  if (o1==null) return -1;
	      if (o2==null) return 1;
		  return o1.compareTo(o2);
		}  
	  };
	  
	  public static final Comparator<String> DEFAULT_VERSION_LONG_COMPARATOR = new Comparator<String>(){
		@Override
		public int compare(String o1, String o2) {
		  long l1,l2;
		  if (o1==null || o1.length()==0){
		    l1=0L;
		  }
		  else{
			l1=Long.parseLong(o1);
		  }
		  if (o2==null || o2.length()==0){
		    l2=0L;
		  }
		  else{
			l2=Long.parseLong(o2);
		  }
		  return Long.valueOf(l1).compareTo(Long.valueOf(l2));
		}  
	  };
}
