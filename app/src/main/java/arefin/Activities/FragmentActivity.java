package arefin.Activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.batfia.arefin.ManagerX.R;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import arefin.Database.Attendee;
import arefin.Database.AttendeeDB;
import arefin.Database.EventDB;
import arefin.Database.MenuItemDB;
import arefin.Database.Order;
import arefin.Database.OrderDB;
import arefin.app;
import arefin.dialogs.fragment.SimpleDialogFragment;
import arefin.dialogs.iface.IMultiChoiceListDialogListener;
import arefin.dialogs.iface.ISimpleDialogListener;

public class FragmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener ,IMultiChoiceListDialogListener, OrderFragment.onOrdered,ISimpleDialogListener {

    DrawerLayout drawer;
    int itemNum;
    double[] priceList;
    String[] descList,users;
    List<String> userlist;
    OrderFragment fragments[];
    PaymentFragment paymentFragment;
    int[] fragment_id;
    Adapter adapter;
    int eventID;
    ArrayList<arefin.Database.MenuItem> menuItemList;

    public void onitemOrdered(String name,int frag_id,Order order)
    {
        OrderDB.insertOrder(order);
        paymentFragment.addOrder(name,priceList[frag_id]);
    }

    public void onitemRemoved(String name,int frag_id,int orderID)
    {
        OrderDB.deleteOrderbyID(orderID);
        paymentFragment.removeOrder(name,priceList[frag_id]);
    }

    @Override
    public void onListItemsSelected(CharSequence[] values, int[] selectedPositions, int choice) {

        Log.i("fahimOrder","Items Selected ");
        int check,frag;
        check=choice%10;
        frag=(choice-check)/10;
        fragments[frag].onListItemsSelected(values,selectedPositions,check);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        eventID= app.currentEventID;

        userlist=new ArrayList<>();

        retrieve_sharedArray();
        fragments=new OrderFragment[itemNum];
        fragment_id=new int[itemNum];
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_item);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.setItemIconTintList(null);

        final android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    public void onBackPressed()
    {
        savePreference();
        //saveSharedPreferences();
        Intent i = new Intent(FragmentActivity.this, StartActivity.class);
        // set the new task and clear flags
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public void savePreference()
    {
        for(int i=0;i<itemNum;i++) {
            fragments[i].backUpSelected();
        }
        paymentFragment.updatetoDB();
    }

    public void retrieve_sharedArray()
    {
        menuItemList= MenuItemDB.getItemsByEvent(eventID);
        if(menuItemList==null)
            return;
        itemNum = menuItemList.size();
        descList = new String[itemNum];
        priceList=new double[itemNum];
        for(int i=0; i<itemNum; i++)
        {
            descList[i] = menuItemList.get(i).description;
            priceList[i] = menuItemList.get(i).price;
        }
        userlist=new ArrayList<>();
        ArrayList<Attendee> attendeeList= AttendeeDB.getAttendeesByEvent(eventID);
        for(int i=0;i<attendeeList.size();i++)
        {
            userlist.add(attendeeList.get(i).name);
        }
        users=new String[userlist.size()];
        for (int i = 0; i < userlist.size(); i++) {
            users[i]=userlist.get(i);
        }

        //Retrieve Saved Orders
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i("FahimOrders","Resume");
        for(int i=0;i<itemNum;i++)
            fragments[i].updateView(i);
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

           /* case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;*/

            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_menu_edit:
                Intent createIntent = new Intent(FragmentActivity.this, MenuEditActivity.class);
                startActivity(createIntent);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @SuppressWarnings("StatementWithEmptyBody")



    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager=getFragmentManager();
        if (id == R.id.nav_about)
        {
            Intent createIntent = new Intent(FragmentActivity.this, CreditsActivity.class);
            startActivity(createIntent);
        }
        else if (id == R.id.nav_feedback) {
            drawer.closeDrawers();
            Uri uri = Uri.parse("https://fahim6119.wordpress.com/contact/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        else if (id == R.id.nav_exit) {
            finish();
        }
        else if (id == R.id.nav_history)
        {
            Intent createIntent = new Intent(FragmentActivity.this, HistoryActivity.class);
            startActivity(createIntent);
            finish();
        }
        else if (id == R.id.nav_create)
        {
            savePreference();
            Intent createIntent = new Intent(FragmentActivity.this, CreateActivity.class);
            startActivity(createIntent);
            finish();
        }
        else if (id == R.id.nav_members)
        {
            Intent createIntent = new Intent(FragmentActivity.this, AttendanceActivity.class);
            createIntent.putExtra("Mode",1);
            startActivity(createIntent);
            finish();
        }

        else if (id == R.id.nav_menu)
        {
            Intent createIntent = new Intent(FragmentActivity.this, MenuCreatorActivity.class);
            createIntent.putExtra("Mode",2);
            startActivity(createIntent);
            finish();
        }

        else if (id == R.id.nav_order)
        {
            SimpleDialogFragment.createBuilder(this,
                    getSupportFragmentManager()).setTitle("Warning!")
                    .setMessage("You want to reset all the orders?")
                    .setPositiveButtonText("Yes")
                    .setNegativeButtonText("No")
                    .setCancelable(true)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {

    }

    @Override
    public void onNeutralButtonClicked(int requestCode) {

    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        OrderDB.deleteOrderbyEvent(eventID);
        ArrayList<Attendee> attendees= AttendeeDB.getAttendeesByEvent(eventID);
        for(int i=0;i<attendees.size();i++)
        {
            attendees.get(i).total=0;
            AttendeeDB.update(attendees.get(i));
        }

        Intent createIntent = new Intent(FragmentActivity.this, ItemListActivity.class);
        startActivity(createIntent);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());
        for(int i=0;i<itemNum;i++) {
            Bundle bundle = new Bundle();
            bundle.putInt("ItemSerial",menuItemList.get(i).serial );
            bundle.putInt("fragId",i);
            fragments[i]=new OrderFragment();
            adapter.addFragment(fragments[i], descList[i]);
            adapter.getItem(i).setArguments(bundle);
        }
        paymentFragment=new PaymentFragment();
        adapter.addFragment(paymentFragment,"Payment ");
        viewPager.setAdapter(adapter);
    }

    public void saveEvent(View v)
    {
        Log.i("checkLog","Finished");
        onBackPressed();
    }

    public void addVat(View v)
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_percentage, null);
        dialog.setContentView(dialogView);

        dialog.setCancelable(true);

        final Button percentageSetButton = (Button) dialogView.findViewById(R.id.percentageSetButton);
        final EditText vat = (EditText) dialogView.findViewById(R.id.vat);
        final EditText discount = (EditText) dialogView.findViewById(R.id.discount);

        final Button percentageCancelButton=(Button) dialogView.findViewById(R.id.percentageCancel);
        percentageCancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        percentageSetButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                String vatPercent= vat.getText().toString().trim();
                String discountPercent=discount.getText().toString().trim();
                double vat,discount;
                if(TextUtils.isEmpty(vatPercent))
                {
                    vat=0;
                }
                else
                    vat=Double.parseDouble(vatPercent);
                if(TextUtils.isEmpty(discountPercent))
                {
                    discount=0;
                }
                else
                    discount=Double.parseDouble(discountPercent);
                paymentFragment.addVat(vat,discount);
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    public void exportHistory(View v)
    {
        savePreference();
        //SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Log.i("FahimFile","File Writing began");
        String name= EventDB.getEventByID(eventID).name;
        String date= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // Create Folder
        //String folder_main = getBaseContext().getString(R.string.app_name);
        String folder_main="ManagerX";
        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }

        Log.i("checkLog","folder created");
        File myPath = new File(Environment.getExternalStorageDirectory()+ "/" + folder_main);
        File myFile = new File(myPath, name+"_"+date+".txt");
        Log.i("checkLog","file created");
        try
        {
            FileWriter fw = new FileWriter(myFile);
            PrintWriter pw = new PrintWriter(fw);
            SavedEvent savedEvent =new SavedEvent(name);
            pw.println(savedEvent.toString());
            pw.close();
            fw.close();
            Toast.makeText(getBaseContext(), "Records of Event " + name + " exported to SD Card",
                    Toast.LENGTH_LONG).show();
            onBackPressed();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
