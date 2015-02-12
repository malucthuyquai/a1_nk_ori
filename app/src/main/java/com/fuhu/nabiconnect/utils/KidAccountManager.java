package com.fuhu.nabiconnect.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.fuhu.account.AccountParser;
import com.fuhu.account.data.Kid;
import com.fuhu.nabiconnect.log.LOG;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

public class KidAccountManager {

	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "KidAccountManager";
	public static final String KID_ID_KEY = "Kid ID";	
	
	/*======================================================================
	 * Static Fields
	 *=======================================================================*/
	private static Hashtable<Long, Drawable> photoTable = new Hashtable<Long, Drawable>();
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	//private AccountManager m_AccountManager;
	private static Kid m_CurrentKid;
	private ArrayList<Kid> m_KidList;
	//private NetworkManager m_NetworkManager;

	private class LoadAllKidsPhotoTask extends AsyncTask<Object, Object, Object> {
		
		@Override
		protected Object doInBackground(Object... arg0) {	
			if(m_KidList != null) {
				for(Kid kid : m_KidList)
					loadKidPhoto(kid);
			}
			else {
				LOG.W(TAG,"doInBackground() - m_KidList is null");
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {	
			super.onPostExecute(result);
		}
	}
	
	/*======================================================================
	 * Constructor
	 *=======================================================================*/
	public KidAccountManager(Context context)
	{
		this(context,false);
	}
	public KidAccountManager(Context context, boolean preloadData)
	{
		this.m_Context = context;
		//this.m_AccountManager = am;
		//this.m_NetworkManager = new NetworkManager(context);
		this.init(preloadData);
	}
	
	/*======================================================================
	 * Initialize
	 *=======================================================================*/
	private void init(boolean preload)
	{
		//if(m_AccountManager != null)
		{
			try {
				
				// get kid list
				if(AccountParser.getAccount(m_Context) != null)
					m_KidList = AccountParser.getAccount(m_Context).getKidList();
				
				// get current kid
				if(m_CurrentKid == null || !isKidInKidList(m_KidList , m_CurrentKid))
				{						
					m_CurrentKid = AccountParser.getAccount(m_Context).getCurrentKid();
					if(m_CurrentKid != null)
						LOG.V(TAG, "m_CurrentKid is reloaded. kid id : "+m_CurrentKid.getKidId());
				}
								
				// preload all kid's photo
				if(preload)
				{
					LoadAllKidsPhotoTask loadPhotoTasks = new LoadAllKidsPhotoTask();
					loadPhotoTasks.execute();
				}
			} catch (Exception e) {
				LOG.E(TAG,"init() -  Falied to initialized kid data.");
			}
		}
		//else
		//{
		//	LOG.E(TAG,"init() -  m_AccountManager is null");
		//}
	}
	
	/*======================================================================
	 * Check if kid is in kid list
	 *=======================================================================*/
	private boolean isKidInKidList(ArrayList<Kid> kidList, Kid kid)
	{
		if(kidList == null)
		{
			LOG.E(TAG,"isKidInKidList() -  kidList is null");
			return false;
		}
		
		if(kid == null)
		{
			LOG.E(TAG,"isKidInKidList() -  kid is null");
			return false;
		}
		
		boolean isInList = false;
		
		for(Kid kidInList : kidList)
		{
			if(kidInList != null)
				LOG.V(TAG,"kidInList.getKidId() is "+kidInList.getKidId()); 
			
			if(kidInList != null && kidInList.getKidId() == kid.getKidId())
			{
				isInList = true;
				break;
			}
		}
		
		LOG.V(TAG,"isKidInKidList() -  kid "+kid.getKidId()+" in list : "+isInList);
		
		return isInList;
	}
	
	

	
	/*======================================================================
	 * Load photo of current kid
	 *=======================================================================*/
	public Drawable loadKidPhoto(Kid kid)
	{
		if(kid == null)
		{
			LOG.W(TAG,"loadKidPhoto() - kid is null");
			return null;
		}
		
		Drawable kidPhoto = null;

		try {
            InputStream is = (InputStream) new URL(kid.getKidPhotos().getNum128()).getContent();
            kidPhoto = Drawable.createFromStream(is, "src name");
            
            // add drawable into cache
            photoTable.put(kid.getKidId(), kidPhoto);
		} catch (Throwable e) {
			LOG.E(TAG,"loadKidPhoto() - failed to load kid's photo.");
		}
		return kidPhoto;
	}
	
	/*======================================================================
	 * Get current Kid
	 *=======================================================================*/
	public Kid getCurrentKid()
	{
		return KidAccountManager.m_CurrentKid;
	}
	
	/*======================================================================
	 * Get nabi current Kid
	 *=======================================================================*/
	public Kid getNabiCurrentKid()
	{
		//if(m_AccountManager != null)
			try {
				return AccountParser.getAccount(m_Context).getCurrentKid();
			} catch (Exception e) {
				LOG.E(TAG,"getNabiCurrentKid() - failed to getCurrentKid",e);
			}
		//else
		//{
		//	LOG.E(TAG,"getNabiCurrentKid() - m_AccountManager is null");
		//	return KidAccountManager.m_CurrentKid;
		//}
		
		return KidAccountManager.m_CurrentKid;
	}
	
	/*======================================================================
	 * Get kid list
	 *=======================================================================*/
	public ArrayList<Kid> getKidList()
	{
		return this.m_KidList;
	}
	/*======================================================================
	 * Get Kid by ID
	 *=======================================================================*/
	public Kid getKidById(Long kidId)
	{
		//if(m_AccountManager != null)
		//{
			try {
				return AccountParser.getAccount(m_Context).getKidById(kidId);
			} catch (Exception e) {
				LOG.E(TAG,"getKidById() - failed to getKidById",e);
			}
		//}
		//LOG.W(TAG,"getKidById() - m_AccountManager is null");
		return null;
	}
	
	/*======================================================================
	 * Set current Kid
	 *=======================================================================*/
	public void setCurrentKid(Kid kid)
	{
		if(kid != null)
			LOG.V(TAG, "setCurrentKid() - kid has been set to "+kid.getKidId());
		else
			LOG.W(TAG, "setCurrentKid() - kid has been set to null");
		KidAccountManager.m_CurrentKid = kid;
	}
	
	/*======================================================================
	 * Deinitialize
	 *=======================================================================*/
	public void deInit()
	{
		m_Context = null;
		//m_AccountManager = null;
		//photoTable.clear();
	}

}
