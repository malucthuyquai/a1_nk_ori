package com.fuhu.nabiconnect;

import com.fuhu.nabiconnect.mail.MailActivity.ReplyReceiverData;

// The container Activity must implement this interface so the frag can deliver messages
public interface IOnMainBarItemSelectedListener {
    /** Called by HeadlinesFragment when a list item is selected */
    public void OnMainBarItemSelected(int position);
    public void OnMainBarItemSelected(int position, ReplyReceiverData data);
}
