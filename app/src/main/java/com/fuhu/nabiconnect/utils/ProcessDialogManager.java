package com.fuhu.nabiconnect.utils;

import android.app.ProgressDialog;
import android.content.Context;

import com.fuhu.nabiconnect.R;

public class ProcessDialogManager {
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private ProgressDialog m_ProgressDialog;
	
	/*======================================================================
	 * Constructor
	 *=======================================================================*/
	public ProcessDialogManager(Context context)
	{
		this.m_Context = context;	
		m_ProgressDialog = new ProgressDialog(context);
		m_ProgressDialog.setMessage(context.getString(R.string.util_connecting));
		m_ProgressDialog.setCancelable(false);
	}
	
	public void showDialog()
	{
		if(!m_ProgressDialog.isShowing())
			m_ProgressDialog.show();
	}
	
	public void dismissDialog()
	{
		if(m_ProgressDialog.isShowing())
			m_ProgressDialog.dismiss();
	}

}
