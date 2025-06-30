package com.tengu.sharetoclipboard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ShareToOtherAppsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent baseIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            baseIntent = getIntent().getParcelableExtra("intent", Intent.class);
        } else {
            // fallback
            baseIntent = getIntent().getParcelableExtra("intent");
        }

        if (baseIntent == null) {
            finish();
            return;
        }

        Bundle bundle = baseIntent.getExtras();
        if (bundle == null) {
            finish();
            return;
        }

        Intent intent = baseIntent.cloneFilter();

        for (String key : bundle.keySet()) {
            if (bundle.get(key) instanceof String) {
                intent.putExtra(key, (String) bundle.get(key));
            } else {
                intent.putExtra(key, String.valueOf(bundle.get(key)));
            }
        }
        shareExceptCurrentApp(intent);

        finish();
    }

    private void shareExceptCurrentApp(Intent intent) {
        intent.setPackage(null);
        intent.setComponent(null);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activities = packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0));
        } else {
            // fallback
            activities = packageManager.queryIntentActivities(intent, 0);
        }

        String packageNameToHide = getPackageName();
        ArrayList<Intent> targetIntents = new ArrayList<>();
        for (ResolveInfo currentInfo : activities) {
            String packageName = currentInfo.activityInfo.packageName;
            if (!packageNameToHide.equals(packageName)) {
                Intent targetIntent = new Intent(intent);
                targetIntent.setPackage(packageName);
                targetIntents.add(targetIntent);
            }
        }
        if (targetIntents.size() > 0) {
            Intent chooserIntent = Intent.createChooser(targetIntents.remove(0),
                    getString(R.string.share_chooser_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    targetIntents.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        }
    }
}
