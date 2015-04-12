package io.github.sfanxiang.tcpsender;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    Fragment fragments[] = {
            SendFragment.newInstance(1),
            LogFragment.newInstance(2)
    };
    int curPosition = 0;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_section1);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        FragmentManager fragmentManager = getFragmentManager();
        for (int i = 0; i < fragments.length; i++) {
            fragmentManager.beginTransaction().add(R.id.container, fragments[i]).commit();
            if (i != 0) fragmentManager.beginTransaction().hide(fragments[i]).commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        android.os.Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Spinner spinner = (Spinner) SendFragment.rootView.findViewById(R.id.spinner);
        String str[] = new String[spinner.getCount()];
        for (int i = 0; i < spinner.getCount(); i++) {
            str[i] = (String) spinner.getItemAtPosition(i);
        }
        outState.putStringArray("spinner", str);
        outState.putInt("spinner_sel", spinner.getSelectedItemPosition());

        spinner = (Spinner) SendFragment.rootView.findViewById(R.id.spinner_Newline);
        outState.putInt("spinner_Newline_sel", spinner.getSelectedItemPosition());

        EditText editText;
        editText = (EditText) SendFragment.rootView.findViewById(R.id.editText_Name);
        outState.putString("editText_Name", editText.getText().toString());
        editText = (EditText) SendFragment.rootView.findViewById(R.id.editText_Host);
        outState.putString("editText_Host", editText.getText().toString());
        editText = (EditText) SendFragment.rootView.findViewById(R.id.editText_Message);
        outState.putString("editText_Message", editText.getText().toString());

        TextView textView;
        textView = (TextView) LogFragment.rootView.findViewById(R.id.textView_Log);
        outState.putString("textView_Log", textView.getText().toString());

        outState.putInt("curPosition", curPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        String str[] = savedInstanceState.getStringArray("spinner");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (SendFragment.rootView.getContext(), android.R.layout.simple_spinner_item, str);
        Spinner spinner = (Spinner) SendFragment.rootView.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(savedInstanceState.getInt("spinner_sel", 0));

        spinner = (Spinner) SendFragment.rootView.findViewById(R.id.spinner_Newline);
        spinner.setSelection(savedInstanceState.getInt("spinner_Newline_sel", 0));

        EditText editText;
        editText = (EditText) SendFragment.rootView.findViewById(R.id.editText_Name);
        editText.setText(savedInstanceState.getString("editText_Name", ""));
        editText = (EditText) SendFragment.rootView.findViewById(R.id.editText_Host);
        editText.setText(savedInstanceState.getString("editText_Host", ""));
        editText = (EditText) SendFragment.rootView.findViewById(R.id.editText_Message);
        editText.setText(savedInstanceState.getString("editText_Message", ""));

        TextView textView;
        textView = (TextView) LogFragment.rootView.findViewById(R.id.textView_Log);
        textView.setText(savedInstanceState.getString("textView_Log", ""));

        onNavigationDrawerItemSelected(savedInstanceState.getInt("curPosition", 0));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        curPosition = position;
        onSectionAttached(position + 1);

        FragmentManager fragmentManager = getFragmentManager();

        for (int i = 0; i < fragments.length; i++) {
            fragmentManager.beginTransaction().hide(fragments[i]).commit();
        }
        fragmentManager.beginTransaction().show(fragments[position]).commit();

        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            if (curPosition == 0)
                getMenuInflater().inflate(R.menu.send, menu);
            else
                getMenuInflater().inflate(R.menu.log, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Toast.makeText(this, R.string.about_string, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_load) {
            Message msg = SendFragment.handler.obtainMessage();
            msg.what = SendFragment.Messages.ACTION_LOAD.ordinal();
            msg.sendToTarget();
        } else if (id == R.id.action_save) {
            Message msg = SendFragment.handler.obtainMessage();
            msg.what = SendFragment.Messages.ACTION_SAVE.ordinal();
            msg.sendToTarget();
        } else if (id == R.id.action_delete) {
            Message msg = SendFragment.handler.obtainMessage();
            msg.what = SendFragment.Messages.ACTION_DELETE.ordinal();
            msg.sendToTarget();
        } else if (id == R.id.action_reset) {
            Message msg = SendFragment.handler.obtainMessage();
            msg.what = SendFragment.Messages.ACTION_RESET.ordinal();
            msg.sendToTarget();
        } else if (id == R.id.action_copy) {
            Message msg = LogFragment.handler.obtainMessage();
            msg.what = LogFragment.Messages.ACTION_COPY.ordinal();
            msg.sendToTarget();
        } else if (id == R.id.action_clear) {
            Message msg = LogFragment.handler.obtainMessage();
            msg.what = LogFragment.Messages.ACTION_CLEAR.ordinal();
            msg.sendToTarget();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SendFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        public static Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Messages.SET_BUTTON_TEXT_RES.ordinal()) {
                    ((Button) (rootView.findViewById(msg.arg1))).setText(msg.arg2);
                } else if (msg.what == Messages.SHOW_TOAST_RES.ordinal()) {
                    Toast.makeText(rootView.getContext(), msg.arg1, msg.arg2).show();
                } else if (msg.what == Messages.SHOW_TOAST_STRING.ordinal()) {
                    Toast.makeText(rootView.getContext(), (String) (msg.obj), msg.arg2).show();
                } else if (msg.what == Messages.ACTION_LOAD.ordinal()) {
                    SharedPreferences record = rootView.getContext().getSharedPreferences("record", MODE_PRIVATE);
                    Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);

                    String curName = (String) spinner.getSelectedItem();

                    EditText edit = (EditText) rootView.findViewById(R.id.editText_Name);
                    edit.setText(curName);
                    edit = (EditText) rootView.findViewById(R.id.editText_Host);
                    edit.setText(record.getString("host_" + curName, ""));

                    edit = (EditText) rootView.findViewById(R.id.editText_Message);
                    String message = record.getString("message_" + curName, "");
                    edit.setText(message.substring(0, Math.min(record.getInt("messagelen_" + curName, 0), message.length())));

                    spinner = (Spinner) rootView.findViewById(R.id.spinner_Newline);
                    spinner.setSelection(record.getInt("newline_" + curName, 0), true);
                } else if (msg.what == Messages.ACTION_SAVE.ordinal()) {
                    Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
                    SpinnerAdapter adapter = spinner.getAdapter();
                    Set<String> nameSet = new HashSet<String>();
                    for (int i = 0; i < adapter.getCount(); i++)
                        nameSet.add((String) adapter.getItem(i));

                    String curName = ((EditText) rootView.findViewById(R.id.editText_Name)).getText().toString();
                    nameSet.add(curName);

                    adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item,
                            nameSet.toArray(new String[nameSet.size()]));
                    spinner.setAdapter(adapter);

                    for (int i = 0; i < spinner.getCount(); i++) {
                        if (((String) spinner.getItemAtPosition(i)).equals(curName)) {
                            spinner.setSelection(i, true);
                            break;
                        }
                    }

                    SharedPreferences record = rootView.getContext().getSharedPreferences("record", MODE_APPEND);
                    SharedPreferences.Editor editor = record.edit();
                    editor.putStringSet("name", nameSet);
                    editor.putString("host_" + curName, ((EditText) rootView.findViewById(R.id.editText_Host)).getText().toString());

                    String message = ((EditText) rootView.findViewById(R.id.editText_Message)).getText().toString();
                    message = message.replace("\r", "");
                    editor.putInt("messagelen_" + curName, message.length());
                    editor.putString("message_" + curName, message);

                    editor.putInt("newline_" + curName, ((Spinner) rootView.findViewById(R.id.spinner_Newline)).getSelectedItemPosition());
                    editor.commit();
                } else if (msg.what == Messages.ACTION_DELETE.ordinal()) {
                    Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
                    Set<String> nameSet = new HashSet<>();

                    String curName = (String) spinner.getSelectedItem();

                    for (int i = 0; i < spinner.getCount(); i++) {
                        if (i != spinner.getSelectedItemPosition())
                            nameSet.add((String) spinner.getItemAtPosition(i));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item,
                            nameSet.toArray(new String[nameSet.size()]));
                    spinner.setAdapter(adapter);

                    SharedPreferences record = rootView.getContext().getSharedPreferences("record", MODE_APPEND);
                    SharedPreferences.Editor editor = record.edit();
                    editor.putStringSet("name", nameSet);
                    editor.remove("host_" + curName);
                    editor.remove("messagelen_" + curName);
                    editor.remove("message_" + curName);
                    editor.remove("newline_" + curName);
                    editor.commit();
                } else if (msg.what == Messages.ACTION_RESET.ordinal()) {
                    EditText editText = (EditText) rootView.findViewById(R.id.editText_Name);
                    editText.setText("");
                    editText = (EditText) rootView.findViewById(R.id.editText_Host);
                    editText.setText("");
                    editText = (EditText) rootView.findViewById(R.id.editText_Message);
                    editText.setText("");
                    Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner_Newline);
                    spinner.setSelection(0);
                }
            }
        };

        private static Runnable connectRun = new Runnable() {
            @Override
            public void run() {
                try {
                    String host = ((EditText) (rootView.findViewById(R.id.editText_Host))).getText().toString();
                    int spl = host.indexOf(':');
                    String hostAddr;
                    int hostPort;
                    if (spl != -1) {
                        hostAddr = host.substring(0, spl);
                        hostPort = Integer.parseInt(host.substring(spl + 1));
                    } else {
                        hostAddr = host;
                        hostPort = 80;
                    }
                    try {
                        SendFragment.socket.close();
                    } catch (Exception e) {
                    }
                    SendFragment.socket = new Socket(hostAddr, hostPort);

                    if (SendFragment.socket.isConnected()) {
                        Message msg = handler.obtainMessage();
                        msg.arg1 = R.string.string_connected;
                        msg.arg2 = Toast.LENGTH_SHORT;
                        msg.what = Messages.SHOW_TOAST_RES.ordinal();
                        msg.sendToTarget();
                    } else {
                        Message msg = handler.obtainMessage();
                        msg.arg1 = R.string.string_connect_fail;
                        msg.arg2 = Toast.LENGTH_SHORT;
                        msg.what = Messages.SHOW_TOAST_RES.ordinal();
                        msg.sendToTarget();
                    }
                } catch (Exception e) {
                    Message msg = handler.obtainMessage();
                    msg.obj = e.toString();
                    msg.arg2 = Toast.LENGTH_SHORT;
                    msg.what = Messages.SHOW_TOAST_STRING.ordinal();
                    msg.sendToTarget();
                }
                SendFragment.connecting = false;

                Message msg = handler.obtainMessage();
                msg.arg1 = R.id.button_Connect;
                msg.arg2 = R.string.string_connect;
                msg.what = Messages.SET_BUTTON_TEXT_RES.ordinal();
                msg.sendToTarget();
            }
        };

        private static Runnable sendRun = new Runnable() {
            @Override
            public void run() {
                try {
                    String str = ((EditText) (rootView.findViewById(R.id.editText_Message))).getText().toString();
                    String newLine = "\n";
                    switch (((Spinner) (rootView.findViewById(R.id.spinner_Newline))).getSelectedItemPosition()) {
                        case 0:
                            newLine = "\n";
                            break;
                        case 1:
                            newLine = "\r\n";
                            break;
                        case 2:
                            newLine = "\r";
                            break;
                    }
                    str = str.replace(System.getProperty("line.separator"), newLine);
                    SendFragment.socket.getOutputStream().write(str.getBytes());
                    SendFragment.socket.getOutputStream().flush();

                    Message msg = handler.obtainMessage();
                    msg.arg1 = R.string.string_sent;
                    msg.arg2 = Toast.LENGTH_SHORT;
                    msg.what = Messages.SHOW_TOAST_RES.ordinal();
                    msg.sendToTarget();

                    msg = LogFragment.handler.obtainMessage();
                    msg.what = LogFragment.Messages.APPEND_LOG.ordinal();
                    msg.obj = "From " + SendFragment.socket.getLocalSocketAddress()
                            + "\nTo " + SendFragment.socket.getRemoteSocketAddress()
                            + ":\n" + str + "\n\n";
                    msg.sendToTarget();
                } catch (Exception e) {
                    Message msg = handler.obtainMessage();
                    msg.obj = e.toString();
                    msg.arg2 = Toast.LENGTH_SHORT;
                    msg.what = Messages.SHOW_TOAST_STRING.ordinal();
                    msg.sendToTarget();
                }
                SendFragment.sending = false;

                Message msg = handler.obtainMessage();
                msg.arg1 = R.id.button_Send;
                msg.arg2 = R.string.string_send;
                msg.what = Messages.SET_BUTTON_TEXT_RES.ordinal();
                msg.sendToTarget();
            }
        };

        private static Runnable receiveRun = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        SystemClock.sleep(1000);
                        byte tmp[] = new byte[100000];
                        int size;
                        if ((size = SendFragment.socket.getInputStream().read(tmp, 0, 99999)) != -1) {
                            Message msg = LogFragment.handler.obtainMessage();
                            msg.what = LogFragment.Messages.APPEND_LOG.ordinal();
                            msg.obj = "From " + socket.getRemoteSocketAddress().toString()
                                    + "\nTo " + socket.getLocalSocketAddress().toString()
                                    + ":\n" + (new String(Arrays.copyOf(tmp, size))) + "\n\n";
                            msg.sendToTarget();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        };

        private static Socket socket;
        private static View rootView;
        private static boolean connecting = false, sending = false;
        private static Thread connectThread, sendThread, receiveThread;

        public SendFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SendFragment newInstance(int sectionNumber) {
            SendFragment fragment = new SendFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 final Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_send, container, false);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item,
                    new String[]{"LF", "CRLF", "CR"});
            Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner_Newline);
            spinner.setAdapter(adapter);

            Button button = (Button) rootView.findViewById(R.id.button_Connect);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (connecting) {
                            connectThread.interrupt();
                            ((Button) v).setText(R.string.string_connect);
                            connecting = false;
                        } else {
                            try {
                                connectThread.interrupt();
                            } catch (Exception e) {
                            }
                            connectThread = new Thread(connectRun);
                            connectThread.start();
                            ((Button) v).setText(R.string.string_connecting);
                            connecting = true;
                        }
                    } catch (Exception e) {
                        Toast.makeText(rootView.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            button = (Button) rootView.findViewById(R.id.button_Send);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (sending) {
                            sendThread.interrupt();
                            ((Button) v).setText(R.string.string_send);
                            sending = false;
                        } else {
                            sendThread = new Thread(sendRun);
                            sendThread.start();
                            ((Button) v).setText(R.string.string_sending);
                            sending = true;
                        }
                    } catch (Exception e) {
                        Toast.makeText(rootView.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            receiveThread = new Thread(receiveRun);
            receiveThread.start();

            SharedPreferences record = rootView.getContext().getSharedPreferences("record", MODE_PRIVATE);
            Set<String> nameSet;
            nameSet = record.getStringSet("name", new HashSet<String>());

            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item,
                    nameSet.toArray(new String[nameSet.size()]));
            spinner = (Spinner) rootView.findViewById(R.id.spinner);
            spinner.setAdapter(adapter);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        enum Messages {
            SET_BUTTON_TEXT_RES,
            SHOW_TOAST_RES,
            SHOW_TOAST_STRING,
            ACTION_LOAD,
            ACTION_SAVE,
            ACTION_DELETE,
            ACTION_RESET,
        }
    }

    public static class LogFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        public static Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Messages.APPEND_LOG.ordinal()) {
                    TextView textView = (TextView) rootView.findViewById(R.id.textView_Log);
                    textView.append((String) msg.obj);
                } else if (msg.what == Messages.ACTION_COPY.ordinal()) {
                    ((ClipboardManager) rootView.getContext().getSystemService(CLIPBOARD_SERVICE))
                            .setText(((TextView) rootView.findViewById(R.id.textView_Log)).getText());
                    Toast.makeText(rootView.getContext(), R.string.string_copied, Toast.LENGTH_SHORT).show();
                } else if (msg.what == Messages.ACTION_CLEAR.ordinal()) {
                    ((TextView) rootView.findViewById(R.id.textView_Log)).setText("");
                }
            }
        };
        public static View rootView;

        public LogFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static LogFragment newInstance(int sectionNumber) {
            LogFragment fragment = new LogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_log, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        enum Messages {
            APPEND_LOG,
            ACTION_COPY,
            ACTION_CLEAR,
        }
    }
}
