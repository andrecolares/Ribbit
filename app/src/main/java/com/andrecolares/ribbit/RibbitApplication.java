package com.andrecolares.ribbit;


import android.app.Application;

import com.parse.Parse;

public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "4fUuxD8WergbbsokR1b3jXsZPw6xyXBB1hwlKDbR", "l2BdQOGp4DUpReJVbl9eCXRkfbf2gow4t17oY1ZM");


    }
}


