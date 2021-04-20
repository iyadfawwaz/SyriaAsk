package sy.iyad.syriaask;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    SyriaDiscover discover;
    TextView textView;
    Button reg,sta,sto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tx);
        reg = findViewById(R.id.reg);
        sta = findViewById(R.id.sta);
        sto = findViewById(R.id.sto);

        discover = new SyriaDiscover(this,textView);

        reg.setOnClickListener(v ->
        {
            discover.startRegisterService();
        });
        sta.setOnClickListener(v -> {

            discover.startDiscovery();
            sta.setEnabled(false);
            sto.setEnabled(true);
        });
        sto.setOnClickListener(v -> {
            discover.stopDiscovery();
            sto.setEnabled(false);
            sta.setEnabled(true);
        });
    }

}