package com.derrick.user.scratchofflottery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.derrick.user.scratchofflottery.view.ScratchView;

public class MainActivity extends AppCompatActivity {
    private ScratchView mScratchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScratchView = (ScratchView) findViewById(R.id.scarchView);

        //當刮完時候調用 complete()
        mScratchView.setOnScratchCompleteListener(new ScratchView.OnScratchCompleteListener() {
            @Override
            public void complete() {
                Toast.makeText(getApplicationContext(),"You have almost scratch off  ",Toast.LENGTH_LONG).show();
            }
        });

        mScratchView.setText("恭喜你中獎啦!!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
