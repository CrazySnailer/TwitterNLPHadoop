
/**
* @project Web
* @author Dayong.Shen
* @package isiteam.TwitterNLP.dataPreprocess
* @file DataMerge.java
* 
* @date 2013-10-22-上午8:22:16
* @Copyright 2013 ISI Team of NUDT-版权所有
* 
*/
 
package isiteam.TwitterNLP.dataPreprocess;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import isiteam.TwitterNLP.database.bean.Testset;
import isiteam.TwitterNLP.database.bean.TweetInfo;
import isiteam.TwitterNLP.database.bean.UserInfoAll;
import isiteam.TwitterNLP.database.bean.UserInfo;
import isiteam.TwitterNLP.database.dao.TestsetDao;
import isiteam.TwitterNLP.database.dao.TweetInfoDao;
import isiteam.TwitterNLP.database.dao.UserInfoAllDao;
import isiteam.TwitterNLP.database.dao.UserInfoDao;
import isiteam.TwitterNLP.util.AppContext;
import isiteam.TwitterNLP.util.CharUtil;
import isiteam.TwitterNLP.util.Constant;

import javax.annotation.Resource;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;


/**
 * @project Web
 * @author Dayong.Shen
 * @class DataMerge
 * 
 * @date 2013-10-22-上午8:22:16
 * @Copyright 2013 ISI Team of NUDT-版权所有
 * @Version 1.0.0
 */
@Controller
public class DataMerge {
	private static final Logger log =  LoggerFactory
			.getLogger(DataMerge.class);
	
	@Resource
	private UserInfoDao userInfoDao;
		
	@Resource
	private UserInfoAllDao userInfoAllDao;
	
	@Resource
	private TweetInfoDao tweetInfoDao;
	
	@Resource
	private TestsetDao testsetDao;


	
	public void TweetsMerge(){
		
		List<UserInfoAll> userInfoList=new ArrayList<UserInfoAll>();
		List<TweetInfo> tweetInfoList=new ArrayList<TweetInfo>();
		List<Testset> testsetList=new ArrayList<Testset>();
		
		String condition="friendsCount >100";
		
		userInfoList=userInfoAllDao.getUserInfoListByCondition(condition);
		
		for(UserInfoAll e:userInfoList ){
			tweetInfoList.clear();
			testsetList.clear();
			
			tweetInfoList=tweetInfoDao.getTweetsListbyUserId(e.getUserId());
			
			for(TweetInfo e1:tweetInfoList){
				Testset testSet=new Testset();
				
				if(CharUtil.ChinesePercent(e1.getText())>0.6){
					
					testSet.setContent(e1.getText());
					testSet.setPubTime(e1.getCreatedAt());
					testSet.setUserId(e1.getUserId());
					testSet.setInsertTime(new Timestamp(System.currentTimeMillis()));
					
					testsetList.add(testSet);
				}
				
				
			}
			
			testsetDao.batchSaveTestConList(testsetList, 50);
			
		}
		
	}
	
	
	public void DeleteExtraUserInfo(){
		//获取所有用户的条数
		long userNum=userInfoDao.getUserInfoCount();
		
		log.info("总数据："+userNum);
				
		int batchSize=10000;		    
		int num=(int) Math.ceil( (float) (userNum)/batchSize);
		int cursor=0;
		
		//保存用户的Id列表，根据这个表来合并不同的数据表
				Map<String,Integer> userInfoMap=new HashMap<String,Integer>();
				
				List<UserInfo> userList=new ArrayList<UserInfo>();
				
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//定义格式
			
								
				 for(int i=0;i<num;i++){
					 
				    	cursor=i*batchSize;
				    	
				    	log.info("开始取 "+cursor+" 数据");
				    	
				    	userList.clear();		    	
				    	
				    	userList=userInfoDao.getUserInfoList(cursor,batchSize);
				    	
				    	for(UserInfo e:userList){
				    		
				    		String key=e.getUserId()+"-"+df.format(e.getInsertTime());
				    		//log.info("Key is:"+key);
				    		
				    		if(userInfoMap.containsKey(key)){//包含，则删除
				    			
				    			//userInfoDao.delete(e);
				    			userInfoDao.update(e);
				    			
				    		}else{//不包含
				    			userInfoMap.put(key, 1);
				    		}
				    		
				    		
				    	}
				    	
				    	
				 }//end for
		
	}
	
	/**
	 * @function main
	 * 
	 * @param args
	 * @author Dayong.Shen
	 * @date 2013-10-22-上午8:22:16
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configureAndWatch(Constant.LOG4J_PATH);
		log.info("正在创建数据库连接和缓冲池...");
	    AppContext.initAppCtx();
		log.info("数据库连接已连接！缓冲池已建立");
	
		DataMerge dataMerge=(DataMerge) AppContext.appCtx.getBean("dataMerge");
		
		//dataMerge.TweetsMerge();
		
		dataMerge.DeleteExtraUserInfo();
	}

}
