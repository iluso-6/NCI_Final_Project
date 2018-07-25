package shay.example.com.dart_client.helper_classes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Shay de Barra on 01,April,2018
 * Email:  x16115864@student.ncirl.ie
 */
public class MyFirebaseListener {

    private DatabaseReference databaseReference;

    private ValueEventListener valueEventListener;

    public MyFirebaseListener() {
    }

    public MyFirebaseListener(DatabaseReference databaseReference, ValueEventListener valueEventListener) {
        this.databaseReference = databaseReference;
        this.valueEventListener = valueEventListener;
    }

  public ValueEventListener getListenerForRef(DatabaseReference databaseReference){
        return this.valueEventListener;
  }

    public void removeListenerForRef(DatabaseReference databaseReference) {
        ValueEventListener valueEventListener = getListenerForRef(databaseReference);
        databaseReference.removeEventListener(valueEventListener);
    }


}
