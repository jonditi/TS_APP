package com.pathways_international.ts.ui.job;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

/**
 * Created by android-dev on 10/5/17.
 */

public class SyncJob extends Job {

    static final String TAG = "sync_job_tag";


    @NonNull
    @Override
    protected Result onRunJob(Params params) {
//        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), ))

        return null;
    }
}
