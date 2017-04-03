package ex.com.lifecycleexample;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "HelloWorld";

    private static Context context;
    private Button button;
    private EditText name, surname, age, sex, placeOfBirth, date;
    private TextView textView;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        //WARNING//LeakChecker: static reference
        context = this;
        // compute codice fiscale
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra("name", name.getText().toString()); //WARNING//IntentsChecker: intent is filled with taintedness
                sendIntent.putExtra("surname", surname.getText().toString());//WARNING//IntentsChecker: intent is filled with taintedness
                sendIntent.putExtra("sex", sex.getText().toString());//WARNING//IntentsChecker: intent is filled with taintedness
                sendIntent.putExtra("age", age.getText().toString());//WARNING//IntentsChecker: intent is filled with taintedness
                sendIntent.putExtra("date", date.getText().toString());//WARNING//IntentsChecker: intent is filled with taintedness
                sendIntent.putExtra("place", placeOfBirth.getText().toString());//WARNING//IntentsChecker: intent is filled with taintedness
                sendIntent.setType("text/plain");

                // Verify that the intent will resolve to an activity
                if (sendIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(sendIntent);
            }
        });

        //  As long as the Handler hasn’t been handled before the Activity is destroyed,
        // the chain of references will keep the Activity live in memory and will cause a leak
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText("I'm still alive"); //WARNING//LeakChecker: the handler avoid the Activity to be collected by the GC.
            }
        }, 100000);

    }

    private void initViews(){
        this.textView = (TextView) this.findViewById(R.id.name);//SINK: UserInput
        this.button = (Button) this.findViewById(R.id.goToSecond);//SINK: UserInput
        this.name = (EditText) this.findViewById(R.id.editTextName);//SINK: UserInput
        this.surname = (EditText) this.findViewById(R.id.editTextSurname);//SINK: UserInput
        this.age = (EditText) this.findViewById(R.id.editTextAge);//SINK: UserInput
        this.sex = (EditText) this.findViewById(R.id.editTextSex);//SINK: UserInput
        this.placeOfBirth = (EditText) this.findViewById(R.id.editTextPlaceBirth);//SINK: UserInput
        this.date = (EditText) this.findViewById(R.id.editTextDate);//SINK: UserInput
    }

    /**
     * Write to file the name inserted by the user
     */
    private void writeToFile() {
        if(name == null || name.getText().toString().isEmpty())
            return;

        OutputStreamWriter outputStreamWriter=null;
        try {
            outputStreamWriter = new OutputStreamWriter(this.openFileOutput("MyData.txt", Context.MODE_PRIVATE));//WARNING//CloseResources: when an exception occurs at line 90
            outputStreamWriter.write(name.getText().toString());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString()+ outputStreamWriter.toString());//WARNING//Nullness: when an exception occurs at line 91
            onBugShake();
        }
    }

    private void onBugShake(){
        TelephonyManager tm =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        Intent i=new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra("SUBJECT","Exception Occurred");
        i.putExtra("BODY","Exception occured on the following context: device: "+ Build.DEVICE+ ", code: "+ tm.getDeviceId() +", system: "+Build.VERSION.SDK_INT );
        //WARNING//Intents: tainted data filled into an intent
        startActivity(i);
    }

    /**
     * Read from file the name
     * @return the name
     */
    private String readFromFile(){
        String ret = null;
        StringBuilder stringBuilder=null;
        try {
            InputStream inputStream = this.openFileInput("MyData.txt");//WARNING//CloseResource: ResourceNotClose when an exception occurs at line 120 before being closed

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);//WARNING//CloseResource
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//WARNING//CloseResource
                String receiveString = "";
                stringBuilder= new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();//WARNING//CloseResource when an exception occurs at line 120
            }
        }
        catch (IOException f) {
            Log.e(TAG, "File not found: " + f.toString());
            onBugShake();
        }
        ret = stringBuilder.toString();//WARNING//Nullness: when an exception happens in the try block.
        return ret;
    }


    /**
     * Write to file the name inserted by the user
     */
    @Override
    protected void onPause() {
        super.onPause();
        writeToFile();
    }

    /**
     * Read from file the name
     */
    @Override
    protected void onResume() {
        super.onResume();
        String inputFile = readFromFile();
        int length = inputFile.length();
        Log.i(TAG, "On Resume activity");
        Log.i(TAG, "Read test " + inputFile + " " + length);//WARNING//Injection: the data readed from the file are flowing to the Log method.
    }


}
