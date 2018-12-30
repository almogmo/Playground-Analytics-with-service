package com.selina.playgroundanalytics;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class NetworkController {


public static void postPeopleAmount(int size) {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("playground-selina1");

    String key = ref.push().getKey();
    Map<String, Object> map = new HashMap<>();
    map.put(key + "/time", ServerValue.TIMESTAMP);
    map.put(key + "/visitors", size);
    ref.updateChildren(map);
}
}
