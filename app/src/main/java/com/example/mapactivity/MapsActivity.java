package com.example.mapactivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    FirebaseDatabase database;
    DatabaseReference userLocationsRef;
    GoogleMap mMap;
    Context mContext;
    private static final String TAG = "MapsActivity";

    Map<String, Marker> mNamedMarkers = new HashMap<String,Marker>();

    ChildEventListener markerUpdateListener = new ChildEventListener() {

        /**
         * Adds each existing/new location of a marker.
         *
         * Will silently update any existing markers as needed.
         * @param dataSnapshot  The new location data
         * @param previousChildName  The key of the previous child event
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            String key = dataSnapshot.getKey();
            Log.d(TAG, "Adding location for '" + key + "'");

            Double lat = dataSnapshot.child("latitude").getValue(Double.class);
            Double lng = dataSnapshot.child("longitude").getValue(Double.class);
            LatLng location = new LatLng(lat, lng);

            Marker marker = mNamedMarkers.get(key);

            if (marker == null) {
                MarkerOptions options = getMarkerOptions(key);
                marker = mMap.addMarker(options.position(location));
                mNamedMarkers.put(key, marker);
            } else {
                // This marker-already-exists section should never be called in this listener's normal use, but is here to handle edge cases quietly.
                // TODO: Confirm if marker title/snippet needs updating.
                marker.setPosition(location);
            }
        }

        /**
         * Updates the location of a previously loaded marker.
         *
         * Will silently create any missing markers as needed.
         * @param dataSnapshot  The new location data
         * @param previousChildName  The key of the previous child event
         */
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            String key = dataSnapshot.getKey();
            Log.d(TAG, "Location for '" + key + "' was updated.");

            Double lng = dataSnapshot.child("lang").getValue(Double.class);
            Double lat = dataSnapshot.child("lat").getValue(Double.class);
            LatLng location = new LatLng(lat, lng);

            Marker marker = mNamedMarkers.get(key);

            if (marker == null) {
                // This null-handling section should never be called in this listener's normal use, but is here to handle edge cases quietly.
                Log.d(TAG, "Expected existing marker for '" + key + "', but one was not found. Added now.");
                MarkerOptions options = getMarkerOptions(key); // TODO: Read data from database for this marker (e.g. Name, Driver, Vehicle type)
                marker = mMap.addMarker(options.position(location));
                mNamedMarkers.put(key, marker);
            } else {
                // TODO: Confirm if marker title/snippet needs updating.
                marker.setPosition(location);
            }
        }

        /**
         * Removes the marker from its GoogleMap instance
         * @param dataSnapshot  The removed data
         */
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            Log.d(TAG, "Location for '" + key + "' was removed.");

            Marker marker = mNamedMarkers.get(key);
            if (marker != null)
                marker.remove();
        }

        /**
         * Ignored.
         * @param dataSnapshot  The moved data
         * @param previousChildName  The key of the previous child event
         */
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            // Unused
            Log.d(TAG, "Priority for '" + dataSnapshot.getKey()+ "' was changed.");
        }


        /**
         * Error handler when listener is canceled.
         * @param databaseError  The error object
         */
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "markerUpdateListener:onCancelled", databaseError.toException());
            Toast.makeText(mContext, "Failed to load location markers.", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Waits for the map to be ready then loads markers from the database.
     * @param googleMap  The GoogleMap instance
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        database = FirebaseDatabase.getInstance();
        userLocationsRef = database.getReference("User1/Location");

        userLocationsRef.addChildEventListener(markerUpdateListener);



        // later when the activity becomes inactive.
        // userLocationsRef.removeEventListener(markerUpdateListener)
    }

    /**
     * Retrieves the marker data for the given key.
     * @param key  The ID of the marker
     * @return A MarkerOptions instance containing this marker's infoormation
     */
    private MarkerOptions getMarkerOptions(String key) {
        // TODO: Read data from database for the given marker (e.g. Name, Driver, Vehicle type)
        return new MarkerOptions().title("Location placeholder").snippet("Update this with marker information");
    }
}
