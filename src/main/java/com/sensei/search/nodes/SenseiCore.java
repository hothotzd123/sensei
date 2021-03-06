package com.sensei.search.nodes;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;

import proj.zoie.api.DataProvider;
import proj.zoie.api.IndexReaderFactory;
import proj.zoie.api.Zoie;
import proj.zoie.api.ZoieException;
import proj.zoie.api.ZoieIndexReader;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.RuntimeFacetHandlerFactory;
import com.sensei.indexing.api.SenseiIndexPruner;
import com.sensei.indexing.api.SenseiIndexPruner.DefaultSenseiIndexPruner;
import com.sensei.search.jmx.JmxUtil;
import com.sensei.search.req.SenseiSystemInfo;


public class SenseiCore{
  private static final Logger logger = Logger.getLogger(SenseiServer.class);

  private SenseiZoieFactory<?> _zoieFactory;
  private SenseiIndexingManager _indexManager;
  private SenseiQueryBuilderFactory _queryBuilderFactory;
  private final HashSet<Zoie<BoboIndexReader,?>> zoieSystems = new HashSet<Zoie<BoboIndexReader,?>>();
  
  private final int[] _partitions;
  private final int _id;
  private final Map<Integer,Zoie<BoboIndexReader,?>> _readerFactoryMap;
  private SenseiSystemInfo _senseiSystemInfo;
  private volatile boolean _started;
  private SenseiIndexPruner _pruner;
    
  public SenseiCore(int id,int[] partitions,
            SenseiZoieFactory<?> zoieSystemFactory,
            SenseiIndexingManager indexManager,
            SenseiQueryBuilderFactory queryBuilderFactory){

    _zoieFactory = zoieSystemFactory;
    _indexManager = indexManager;
    _queryBuilderFactory = queryBuilderFactory;
    _partitions = partitions;
    _id = id;
    
    _readerFactoryMap = new HashMap<Integer,Zoie<BoboIndexReader,?>>();
    _started = false;
    _pruner = null;
  }
  
  public void setIndexPruner(SenseiIndexPruner pruner){
	_pruner = pruner;
  }
  
  public SenseiIndexPruner getIndexPruner(){
	  return _pruner == null ? new DefaultSenseiIndexPruner() : _pruner;
  }
  
  public int getNodeId(){
    return _id;
  }
  
  public int[] getPartitions(){
    return _partitions;
  }

  public SenseiSystemInfo getSystemInfo()
  {
    if (_senseiSystemInfo == null)
      _senseiSystemInfo = new SenseiSystemInfo();

    //if (_senseiSystemInfo.getClusterInfo() == null)
    //{
      //List<Integer> partitionList = new ArrayList<Integer>(_partitions.length);

      //for (int i=0; i<_partitions.length; ++i)
      //{
        //partitionList.add(_partitions[i]);
      //}

      //Map<Integer, List<Integer>> clusterInfo = new HashMap<Integer, List<Integer>>();
      //clusterInfo.put(_id, partitionList);
      //_senseiSystemInfo.setClusterInfo(clusterInfo);
    //}

    if (_senseiSystemInfo.getFacetInfos() == null)
    {
      Set<SenseiSystemInfo.SenseiFacetInfo> facetInfos = new HashSet<SenseiSystemInfo.SenseiFacetInfo>();
      if (_zoieFactory.getDecorator() != null && _zoieFactory.getDecorator().getFacetHandlerList() != null)
      {
        for (FacetHandler<?> facetHandler : _zoieFactory.getDecorator().getFacetHandlerList())
        {
          facetInfos.add(new SenseiSystemInfo.SenseiFacetInfo(facetHandler.getName()));
        }
      }

      if (_zoieFactory.getDecorator() != null && _zoieFactory.getDecorator().getFacetHandlerFactories() != null)
      {
        for (RuntimeFacetHandlerFactory<?,?> runtimeFacetHandlerFactory : _zoieFactory.getDecorator().getFacetHandlerFactories())
        {
          SenseiSystemInfo.SenseiFacetInfo facetInfo = new SenseiSystemInfo.SenseiFacetInfo(runtimeFacetHandlerFactory.getName());
          facetInfo.setRunTime(true);
          facetInfos.add(facetInfo);
        }
      }
      _senseiSystemInfo.setFacetInfos(facetInfos);
    }

    Date lastModified = new Date(0L);
    String version = null;
    for(Zoie<BoboIndexReader,?> zoieSystem : zoieSystems)
    {
      if (version == null || _zoieFactory.getVersionComparator().compare(version, zoieSystem.getVersion()) < 0)
        version = zoieSystem.getVersion();
      
    }
/*
    for (ObjectName name : _registeredMBeans) {
      try
      {
        Date lastModifiedB = (Date)mbeanServer.getAttribute(name, "LastDiskIndexModifiedTime");
        if (lastModified.compareTo(lastModifiedB) < 0)
          lastModified = lastModifiedB;
      }
      catch (Exception e)
      {
        // Simplely ignore.
      }
    }
    */
    
    // TODO: fix this after zoie/hourglass jmx is done: http://linkedin.jira.com/browse/ZOIE-81
    _senseiSystemInfo.setLastModified(lastModified.getTime());
    if (version != null)
      _senseiSystemInfo.setVersion(version);

    return _senseiSystemInfo;
  }

  public void setSystemInfo(SenseiSystemInfo senseiSystemInfo)
  {
    _senseiSystemInfo = senseiSystemInfo;
  }
  
  public void start() throws Exception{
    if (_started) return;
      for (int part : _partitions){
        
        Zoie<BoboIndexReader,?> zoieSystem = _zoieFactory.getZoieInstance(_id,part);
        
        // register ZoieSystemAdminMBean

        String[] mbeannames = zoieSystem.getStandardMBeanNames();
        for(String name : mbeannames)
        {
          JmxUtil.registerMBean(zoieSystem.getStandardMBean(name), "zoie-name", name + "-" + _id+"-"+part);  
        }
              
        if(!zoieSystems.contains(zoieSystem))
        {
          zoieSystem.start();
          zoieSystems.add(zoieSystem);
        }

        _readerFactoryMap.put(part, zoieSystem);
      }

    logger.info("initializing index manager...");
      _indexManager.initialize(_readerFactoryMap);
      logger.info("starting index manager...");
      _indexManager.start();
      logger.info("index manager started...");
      _started = true;
  }
  
  public void shutdown(){
    if (!_started) return;
    logger.info("unregistering mbeans...");
        // shutdown the index manager

    logger.info("shutting down index manager...");
      _indexManager.shutdown();
    logger.info("index manager shutdown...");
      
        // shutdown the zoieSystems
        for(Zoie<BoboIndexReader,?> zoieSystem : zoieSystems)
        {
          zoieSystem.shutdown();
        }
        zoieSystems.clear();
        _started =false;
  }

  public DataProvider getDataProvider()
  {
    return _indexManager.getDataProvider();
  }
  
  public IndexReaderFactory<ZoieIndexReader<BoboIndexReader>> getIndexReaderFactory(int partition){
    return _readerFactoryMap.get(partition);
  }
  
  public SenseiQueryBuilderFactory getQueryBuilderFactory()
  {
    return _queryBuilderFactory;
  }

  public void syncWithVersion(long timeToWait, String version) throws ZoieException
  {
    _indexManager.syncWithVersion(timeToWait, version);
  }

}
