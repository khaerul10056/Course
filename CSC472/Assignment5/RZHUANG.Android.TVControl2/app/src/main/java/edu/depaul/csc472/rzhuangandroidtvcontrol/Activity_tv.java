package edu.depaul.csc472.rzhuangandroidtvcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Activity_tv extends Activity
        implements CompoundButton.OnCheckedChangeListener {

    private static final int ASK_QUESTION = 100; // request code
    List<FavoriteSettings> favorites = new ArrayList<FavoriteSettings>();
    private int currentChannel = 187; // default channel
    List<Integer> numbers = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);

        // Build favorite list
        favorites.add(new FavoriteSettings(1, "ABC", 100));
        favorites.add(new FavoriteSettings(2, "NBC", 333));
        favorites.add(new FavoriteSettings(3, "CBS", 888));
        // Update favorite button text
        updateFavorite(favorites.get(0));

        //Initial
        final TextView txvPower = (TextView) findViewById(R.id.tvpower);
        txvPower.setText(R.string.tv_off);
        final TextView txvVolume = (TextView) findViewById(R.id.tvvolume);
        txvVolume.setText("50");
        final TextView txvChannel = (TextView) findViewById(R.id.tvchannel);
        txvChannel.setText(String.valueOf(currentChannel));
        changeEnabledControl(false);
        //Power
        Switch switchPower = (Switch) findViewById(R.id.switchpower);
        switchPower.setChecked(false);
        switchPower.setOnCheckedChangeListener(this);
        //Volume
        SeekBar seekVolume = (SeekBar) findViewById(R.id.seekvolume);
        seekVolume.setProgress(50);
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txvVolume.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Channel buttons
        Button btnChannel0 = (Button) findViewById(R.id.channel0);
        Button btnChannel1 = (Button) findViewById(R.id.channel1);
        Button btnChannel2 = (Button) findViewById(R.id.channel2);
        Button btnChannel3 = (Button) findViewById(R.id.channel3);
        Button btnChannel4 = (Button) findViewById(R.id.channel4);
        Button btnChannel5 = (Button) findViewById(R.id.channel5);
        Button btnChannel6 = (Button) findViewById(R.id.channel6);
        Button btnChannel7 = (Button) findViewById(R.id.channel7);
        Button btnChannel8 = (Button) findViewById(R.id.channel8);
        Button btnChannel9 = (Button) findViewById(R.id.channel9);
        Button btnChannelAdd = (Button) findViewById(R.id.channeladd);
        Button btnChannelMinus = (Button) findViewById(R.id.channelminus);
        Button btnChannelFavorite1 = (Button) findViewById(R.id.channelfavorite1);
        Button btnChannelFavorite2 = (Button) findViewById(R.id.channelfavorite2);
        Button btnChannelFavorite3 = (Button) findViewById(R.id.channelfavorite3);

        View.OnClickListener listenerNumber = new View.OnClickListener () {
            public void onClick (View v) {
                int num = Integer.parseInt(((Button) v).getText().toString());
                numbers.add(num);
                if (numbers.size() == 3) {
                    String strChannel = "";
                    for(int item: numbers) {
                        strChannel += String.valueOf(item);
                    }
                    currentChannel = Integer.parseInt(strChannel);
                    setCurrentChannel(currentChannel);
                    numbers.clear();
                }
            }
        };
        // Listener for Add button
        View.OnClickListener listenerAdd = new View.OnClickListener () {
            public void onClick (View v) {
                setCurrentChannel(currentChannel + 1);
                numbers.clear();
            }
        };
        // Listener for Minus button
        View.OnClickListener listenerMinus = new View.OnClickListener () {
            public void onClick (View v) {
                setCurrentChannel(currentChannel - 1);
                numbers.clear();
            }
        };
        // Listener for Favorite button
        View.OnClickListener listeneFavorite1 = new View.OnClickListener () {
            public void onClick (View v) {
                setCurrentChannel(favorites.get(0).channel);
                numbers.clear();
            }
        };
        View.OnClickListener listeneFavorite2 = new View.OnClickListener () {
            public void onClick (View v) {
                setCurrentChannel(favorites.get(1).channel);
                numbers.clear();
            }
        };
        View.OnClickListener listeneFavorite3 = new View.OnClickListener () {
            public void onClick (View v) {
                setCurrentChannel(favorites.get(2).channel);
                numbers.clear();
            }
        };
        //Bind listener to buttons
        btnChannel0.setOnClickListener(listenerNumber);
        btnChannel1.setOnClickListener(listenerNumber);
        btnChannel2.setOnClickListener(listenerNumber);
        btnChannel3.setOnClickListener(listenerNumber);
        btnChannel4.setOnClickListener(listenerNumber);
        btnChannel5.setOnClickListener(listenerNumber);
        btnChannel6.setOnClickListener(listenerNumber);
        btnChannel7.setOnClickListener(listenerNumber);
        btnChannel8.setOnClickListener(listenerNumber);
        btnChannel9.setOnClickListener(listenerNumber);
        btnChannelAdd.setOnClickListener(listenerAdd);
        btnChannelMinus.setOnClickListener(listenerMinus);
        btnChannelFavorite1.setOnClickListener(listeneFavorite1);
        btnChannelFavorite2.setOnClickListener(listeneFavorite2);
        btnChannelFavorite3.setOnClickListener(listeneFavorite3);

        //Intent, to dvr
        Button btnToDvr = (Button) findViewById(R.id.todvr);
        btnToDvr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_tv.this, Activity_dvr.class);
                startActivity(intent);
            }
        });
        //Intent, to config
        Button btnToConfig = (Button) findViewById(R.id.toconfig);
        btnToConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_tv.this, Activity_config.class);
                startActivityForResult(intent, ASK_QUESTION);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASK_QUESTION) {
            if (resultCode == RESULT_OK) {
                // Get parcelable object from config activity
                FavoriteSettings config = data.getExtras().getParcelable("fav");
                updateFavorite(config);
                //updateFavorite(data.getIntExtra("favorite", 0), String.valueOf(data.getCharSequenceExtra("label")), data.getIntExtra("channel", 1));
            }
        }
    }

    private void updateFavorite(FavoriteSettings config) {
        FavoriteSettings fav = null;
        for (FavoriteSettings item: favorites) {
            if (item.id == config.id) {
                fav = item;
                fav.title = config.title;
                fav.channel = config.channel;
                break;
            }
        }
        if (fav != null) {
            Button btnChannelFavorite1 = (Button) findViewById(R.id.channelfavorite1);
            Button btnChannelFavorite2 = (Button) findViewById(R.id.channelfavorite2);
            Button btnChannelFavorite3 = (Button) findViewById(R.id.channelfavorite3);

            btnChannelFavorite1.setText(favorites.get(0).title);
            btnChannelFavorite2.setText(favorites.get(1).title);
            btnChannelFavorite3.setText(favorites.get(2).title);
        }
    }

    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        changeEnabledControl(isChecked);
    }
    private void changeEnabledControl(boolean isChecked) {
        final TextView txvPower = (TextView) findViewById(R.id.tvpower);
        txvPower.setText((isChecked ? R.string.tv_on : R.string.tv_off));
        SeekBar seekVolume = (SeekBar) findViewById(R.id.seekvolume);
        seekVolume.setEnabled(isChecked);
        Button btnChannel0 = (Button) findViewById(R.id.channel0);
        Button btnChannel1 = (Button) findViewById(R.id.channel1);
        Button btnChannel2 = (Button) findViewById(R.id.channel2);
        Button btnChannel3 = (Button) findViewById(R.id.channel3);
        Button btnChannel4 = (Button) findViewById(R.id.channel4);
        Button btnChannel5 = (Button) findViewById(R.id.channel5);
        Button btnChannel6 = (Button) findViewById(R.id.channel6);
        Button btnChannel7 = (Button) findViewById(R.id.channel7);
        Button btnChannel8 = (Button) findViewById(R.id.channel8);
        Button btnChannel9 = (Button) findViewById(R.id.channel9);
        Button btnChannelAdd = (Button) findViewById(R.id.channeladd);
        Button btnChannelMinus = (Button) findViewById(R.id.channelminus);
        Button btnChannelFavorite1 = (Button) findViewById(R.id.channelfavorite1);
        Button btnChannelFavorite2 = (Button) findViewById(R.id.channelfavorite2);
        Button btnChannelFavorite3 = (Button) findViewById(R.id.channelfavorite3);
        btnChannel0.setEnabled(isChecked);
        btnChannel1.setEnabled(isChecked);
        btnChannel2.setEnabled(isChecked);
        btnChannel3.setEnabled(isChecked);
        btnChannel4.setEnabled(isChecked);
        btnChannel5.setEnabled(isChecked);
        btnChannel6.setEnabled(isChecked);
        btnChannel7.setEnabled(isChecked);
        btnChannel8.setEnabled(isChecked);
        btnChannel9.setEnabled(isChecked);
        btnChannelAdd.setEnabled(isChecked);
        btnChannelMinus.setEnabled(isChecked);
        btnChannelFavorite1.setEnabled(isChecked);
        btnChannelFavorite2.setEnabled(isChecked);
        btnChannelFavorite3.setEnabled(isChecked);
        //Set default/highlight color for favorite button
        if (isChecked) {
            btnChannelFavorite1.setTextColor(getResources().getColor(R.color.black));
            btnChannelFavorite2.setTextColor(getResources().getColor(R.color.black));
            btnChannelFavorite3.setTextColor(getResources().getColor(R.color.black));
            if (currentChannel == favorites.get(0).channel) {
                btnChannelFavorite1.setTextColor(getResources().getColor(R.color.yellow));
            }
            else if (currentChannel == favorites.get(1).channel) {
                btnChannelFavorite2.setTextColor(getResources().getColor(R.color.yellow));
            }
            else if (currentChannel == favorites.get(2).channel) {
                btnChannelFavorite3.setTextColor(getResources().getColor(R.color.yellow));
            }
        }
        else
        {
            btnChannelFavorite1.setTextColor(getResources().getColor(R.color.disable));
            btnChannelFavorite2.setTextColor(getResources().getColor(R.color.disable));
            btnChannelFavorite3.setTextColor(getResources().getColor(R.color.disable));
        }
    }

    private void setCurrentChannel(int channel) {
        if (channel < 1 ) {
            channel = 1;
        }
        if (channel > 999) {
            channel = 999;
        }

        currentChannel = channel;
        TextView txvChannel = (TextView) findViewById(R.id.tvchannel);
        txvChannel.setText(String.valueOf(currentChannel));

        //Set text color for the selected button of favorite channel
        Button btnChannelFavorite1 = (Button) findViewById(R.id.channelfavorite1);
        btnChannelFavorite1.setTextColor(getResources().getColor(R.color.black));
        Button btnChannelFavorite2 = (Button) findViewById(R.id.channelfavorite2);
        btnChannelFavorite2.setTextColor(getResources().getColor(R.color.black));
        Button btnChannelFavorite3 = (Button) findViewById(R.id.channelfavorite3);
        btnChannelFavorite3.setTextColor(getResources().getColor(R.color.black));
        if (currentChannel == favorites.get(0).channel) {
            btnChannelFavorite1.setTextColor(getResources().getColor(R.color.yellow));
        }
        else if (currentChannel == favorites.get(1).channel) {
            btnChannelFavorite2.setTextColor(getResources().getColor(R.color.yellow));
        }
        else if (currentChannel == favorites.get(2).channel) {
            btnChannelFavorite3.setTextColor(getResources().getColor(R.color.yellow));
        }
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
