package ru.iammaxim.luciddreamhelper;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidParameterException;

public class MainActivity extends AppCompatActivity {
    EditText timer_et;
    TextView progress_tv;
    ProgressBar progressBar;
    AsyncTask<Integer, Integer, Void> task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer_et = (EditText) findViewById(R.id.timer_tv);
        progress_tv = (TextView) findViewById(R.id.progress_tv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void setProgress(String text, int progress, int max) {
        progress_tv.setText(text);
        progressBar.setMax(max);
        progressBar.setProgress(progress);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start: {
                int waitTime = 0; // time to wait in seconds
                String[] times = timer_et.getEditableText().toString().split(":");
                for (int i = times.length - 1; i >= 0; i--) {
                    waitTime += Integer.parseInt(times[i]) * Math.pow(60, times.length - i - 1);
                }
                if (task != null)
                    task.cancel(true);
                task = new AsyncTask<Integer, Integer, Void>() {
                    final int MODE_WAIT = 0;
                    final int MODE_PLAY = 1;

                    /**
                     * @param toWait seconds to wait
                     */
                    private void _wait(int toWait) throws InterruptedException {
                        int time = 0;
                        publishProgress(MODE_WAIT, time, toWait);
                        while (time < toWait) {
                            Thread.sleep(1000);
                            time++;
                            publishProgress(MODE_WAIT, time, toWait);
                        }
                    }

                    @Override
                    protected Void doInBackground(Integer... params) {
                        if (params.length != 1)
                            throw new InvalidParameterException("Expected 1 long argument, got " + params.length);

                        try {
                            _wait(params[0]);
                            MediaPlayer player = MediaPlayer.create(MainActivity.this, R.raw.wake_up);
                            //play every 5 minutes loop 6 times
                            for (int i = 0; i < 6; i++) {
                                //play every 10 seconds 'Wake up' sound 20 times
                                final int play_times = 20;
                                for (int j = 0; j < play_times; j++) {
                                    publishProgress(MODE_PLAY, j + 1, play_times);
                                    player.start();
                                    Thread.sleep(10000);
                                }
                                _wait(300);
                            }
                        } catch (InterruptedException e) {
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        switch (values[0]) {
                            case MODE_WAIT: {
                                setProgress(values[1] + " seconds out of " + values[2] + " passed", values[1], values[2]);
                                break;
                            }
                            case MODE_PLAY: {
                                setProgress("Playing sound " + values[1] + " out of " + values[2] + " times", values[1], values[2]);
                                break;
                            }
                        }
                    }

                    @Override
                    protected void onCancelled() {
                        setProgress("Cancelled", 0, 1);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        setProgress("Completed", 1, 1);
                    }
                };
                task.execute(waitTime);
                break;
            }
            case R.id.stop: {
                if (task != null)
                    task.cancel(true);
                break;
            }
        }
    }
}