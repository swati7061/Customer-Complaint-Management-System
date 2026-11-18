package com.example.complaintmanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root, content;
    private DB db;
    private String currentEmail = "";
    private String currentName = "";
    private int navy = Color.rgb(23,59,135), blue = Color.rgb(36,87,197);
    private int ink = Color.rgb(32,42,60), muted = Color.rgb(105,116,135);
    private int pale = Color.rgb(242,246,255);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DB(this);
        showWelcome();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius);
        return d;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(color); if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    private Button button(String s, Runnable action) {
        Button b = new Button(this); b.setText(s); b.setAllCaps(false);
        b.setTextColor(Color.WHITE); b.setTextSize(15); b.setBackground(bg(blue, dp(8)));
        b.setPadding(dp(16), dp(12), dp(16), dp(12));
        b.setOnClickListener(v -> action.run()); return b;
    }
    private EditText field(String hint, int inputType) {
        EditText e = new EditText(this); e.setSingleLine(false); e.setTextSize(15);
        e.setHint(hint); e.setInputType(inputType); e.setPadding(dp(16), dp(12), dp(16), dp(12));
        e.setBackground(bg(Color.WHITE, dp(8))); return e;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }
    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private void setup(String title, boolean nav) {
        root = column(); root.setBackgroundColor(pale);
        LinearLayout header = column(); header.setPadding(dp(20), dp(16), dp(20), dp(16)); header.setBackgroundColor(navy);
        TextView brand = text("CUSTOMER CARE  •  COMPLAINT MANAGER", 11, 0xFFD8E4FF, true);
        TextView h = text(title, 24, Color.WHITE, true);
        header.addView(brand); header.addView(h);
        root.addView(header);
        ScrollView scroll = new ScrollView(this);
        content = column(); content.setPadding(dp(16), dp(16), dp(16), dp(24));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        if (nav && !currentEmail.isEmpty()) {
            LinearLayout navBar = row(); navBar.setPadding(dp(4), dp(4), dp(4), dp(4)); navBar.setBackgroundColor(Color.WHITE);
            String[] labels = {"Home", "New Complaint", "My Complaints", "Profile"};
            Runnable[] actions = {this::showHome, this::showNewComplaint, this::showComplaints, this::showProfile};
            for (int i=0;i<labels.length;i++) {
                final Runnable a = actions[i];
                Button b = new Button(this); b.setText(labels[i]); b.setTextSize(10); b.setAllCaps(false);
                b.setTextColor(navy); b.setBackgroundColor(Color.TRANSPARENT);
                b.setOnClickListener(v -> a.run());
                navBar.addView(b, new LinearLayout.LayoutParams(0, dp(50), 1));
            }
            root.addView(navBar);
        }
        setContentView(root);
    }
    private void gap(LinearLayout l, int h) {
        View v = new View(this); l.addView(v, new LinearLayout.LayoutParams(1, dp(h)));
    }
    private void label(LinearLayout l, String s) {
        TextView t = text(s, 14, ink, true); t.setPadding(dp(2), dp(10), dp(2), dp(6)); l.addView(t);
    }
    private void card(LinearLayout parent, View child) {
        LinearLayout wrap = column(); wrap.setPadding(dp(16), dp(14), dp(16), dp(14)); wrap.setBackground(bg(Color.WHITE, dp(12)));
        wrap.addView(child); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(12); parent.addView(wrap, p);
    }
    private void showWelcome() {
        setup("Welcome", false);
        TextView hero = text("A better way to get your issues heard.", 25, ink, true);
        hero.setGravity(Gravity.CENTER); hero.setPadding(dp(8), dp(24), dp(8), dp(12)); content.addView(hero);
        TextView desc = text("Register a complaint, keep track of it, and manage your account in one simple app.", 16, muted, false);
        desc.setGravity(Gravity.CENTER); desc.setPadding(dp(12), 0, dp(12), dp(20)); content.addView(desc);
        TextView icon = text("✓", 48, blue, true); icon.setGravity(Gravity.CENTER); content.addView(icon);
        gap(content, 18);
        Button login = button("Login", this::showLogin);
        content.addView(login, new LinearLayout.LayoutParams(-1, dp(52)));
        gap(content, 10);
        Button reg = button("Create new account", this::showRegister);
        content.addView(reg, new LinearLayout.LayoutParams(-1, dp(52)));
        gap(content, 18);
        TextView note = text("Frontend + local database demo • No online account or server required", 12, muted, false);
        note.setGravity(Gravity.CENTER); content.addView(note);
    }
    private void showLogin() {
        setup("Login", false);
        TextView intro = text("Welcome back! Sign in to manage your complaints.", 15, muted, false); content.addView(intro);
        label(content, "Email address"); EditText email = field("you@example.com", 33); content.addView(email);
        label(content, "Password"); EditText pass = field("Enter password", 129); content.addView(pass);
        gap(content, 18);
        Button b = button("Login", () -> {
            String em=email.getText().toString().trim().toLowerCase(Locale.ROOT), pw=pass.getText().toString();
            String[] user=db.login(em,pw);
            if(user==null) toast("Incorrect email or password. Register first if you are new.");
            else { currentEmail=em; currentName=user[0]; showHome(); }
        });
        content.addView(b, new LinearLayout.LayoutParams(-1, dp(52)));
        gap(content, 10); Button back=button("Create account", this::showRegister);
        content.addView(back, new LinearLayout.LayoutParams(-1, dp(52)));
    }
    private void showRegister() {
        setup("Create Account", false);
        label(content, "Full name"); EditText name=field("Enter your name", 1); content.addView(name);
        label(content, "Email address"); EditText email=field("you@example.com", 33); content.addView(email);
        label(content, "Phone (optional)"); EditText phone=field("Phone number", 3); content.addView(phone);
        label(content, "Password (minimum 4 characters)"); EditText pass=field("Create password", 129); content.addView(pass);
        gap(content, 18);
        Button create=button("Register", () -> {
            String n=name.getText().toString().trim(), e=email.getText().toString().trim().toLowerCase(Locale.ROOT), p=pass.getText().toString();
            if(n.isEmpty() || !e.contains("@") || p.length()<4) { toast("Enter a name, valid email and password of at least 4 characters."); return; }
            if(db.register(n,e,p,phone.getText().toString().trim())) { currentEmail=e; currentName=n; showHome(); }
            else toast("This email is already registered. Please login.");
        });
        content.addView(create, new LinearLayout.LayoutParams(-1, dp(52)));
        gap(content, 10); content.addView(button("Back to login", this::showLogin), new LinearLayout.LayoutParams(-1, dp(52)));
    }
    private void showHome() {
        setup("Hello, " + currentName, true);
        TextView welcome=text("Your concerns matter. What would you like to do today?",15,muted,false);
        welcome.setPadding(dp(2),0,dp(2),dp(16)); content.addView(welcome);
        LinearLayout stats=row();
        int total=db.count(currentEmail), open=db.countOpen(currentEmail);
        stats.addView(statBox("TOTAL COMPLAINTS",String.valueOf(total)),new LinearLayout.LayoutParams(0, dp(90),1));
        View spacer=new View(this); stats.addView(spacer,new LinearLayout.LayoutParams(dp(10),1));
        stats.addView(statBox("IN PROGRESS",String.valueOf(open)),new LinearLayout.LayoutParams(0, dp(90),1));
        content.addView(stats); gap(content,16);
        TextView title=text("Quick actions",18,ink,true); content.addView(title); gap(content,10);
        card(content, actionRow("＋  Register a complaint","Tell us what went wrong.",this::showNewComplaint));
        card(content, actionRow("☷  My complaints","Review complaint details and status.",this::showComplaints));
        card(content, actionRow("♙  My profile","View your account information.",this::showProfile));
        TextView tip=text("Tip: Include clear details so your complaint is easy to understand.",13,muted,false);
        tip.setPadding(dp(8),dp(16),dp(8),dp(8)); content.addView(tip);
    }
    private View statBox(String title,String number) {
        LinearLayout l=column(); l.setPadding(dp(14),dp(12),dp(8),dp(12)); l.setBackground(bg(Color.WHITE, dp(12)));
        l.addView(text(number,26,blue,true)); l.addView(text(title,10,muted,true)); return l;
    }
    private View actionRow(String title,String subtitle,Runnable action) {
        LinearLayout l=column(); l.addView(text(title,16,ink,true)); l.addView(text(subtitle,13,muted,false));
        l.setOnClickListener(v->action.run()); return l;
    }
    private void showNewComplaint() {
        setup("New Complaint", true);
        TextView intro=text("Please provide the details below. Your complaint will be saved on this device.",14,muted,false);
        content.addView(intro); gap(content,8);
        label(content,"Complaint subject"); EditText subject=field("e.g. Defective product",1); content.addView(subject);
        label(content,"Category");
        Spinner category=new Spinner(this);
        String[] cats={"Product defect","Billing / Overcharging","Poor service","Delivery issue","Refund / Return","Other"};
        category.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cats));
        category.setBackground(bg(Color.WHITE, dp(8))); content.addView(category,new LinearLayout.LayoutParams(-1, dp(50)));
        label(content,"Order / Reference number (optional)"); EditText ref=field("Order ID or reference",1); content.addView(ref);
        label(content,"Complaint details"); EditText details=field("Explain the issue, including relevant dates or details...",1);
        details.setMinLines(4); details.setGravity(Gravity.TOP|Gravity.START); content.addView(details);
        gap(content,18);
        Button submit=button("Submit Complaint",()->{
            String s=subject.getText().toString().trim(), d=details.getText().toString().trim();
            if(s.isEmpty()||d.length()<10){toast("Enter a subject and at least 10 characters of details.");return;}
            String id=db.addComplaint(currentEmail,s,cats[category.getSelectedItemPosition()],ref.getText().toString().trim(),d);
            new AlertDialog.Builder(this).setTitle("Complaint registered").setMessage("Your complaint ID is "+id+". You can track it in My Complaints.")
                .setPositiveButton("View complaints",(x,w)->showComplaints()).setNegativeButton("OK",(x,w)->showHome()).show();
        });
        content.addView(submit,new LinearLayout.LayoutParams(-1, dp(52)));
    }
    private void showComplaints() {
        setup("My Complaints", true);
        Cursor c=db.complaints(currentEmail);
        if(c.getCount()==0) {
            TextView empty=text("No complaints yet.",19,ink,true); empty.setGravity(Gravity.CENTER); empty.setPadding(0,dp(35),0,dp(12)); content.addView(empty);
            TextView msg=text("Your registered complaints will appear here.",14,muted,false); msg.setGravity(Gravity.CENTER); content.addView(msg);
            gap(content,18); content.addView(button("Register your first complaint",this::showNewComplaint),new LinearLayout.LayoutParams(-1, dp(52)));
        } else {
            while(c.moveToNext()) {
                String id=c.getString(0), subject=c.getString(1), cat=c.getString(2), status=c.getString(3), date=c.getString(4), details=c.getString(5), ref=c.getString(6);
                LinearLayout box=column(); box.addView(text(id+"  •  "+date,12,muted,true)); gap(box,5);
                box.addView(text(subject,18,ink,true)); gap(box,4);
                box.addView(text(cat,13,muted,false)); gap(box,8);
                TextView badge=text("STATUS: "+status,12,status.equals("Resolved")?Color.rgb(20,125,70):blue,true);
                badge.setPadding(dp(10),dp(5),dp(10),dp(5)); badge.setBackground(bg(status.equals("Resolved")?0xFFE2F7EA:0xFFE6EEFF, dp(12))); box.addView(badge);
                gap(box,8); box.addView(text(details,14,ink,false));
                if(!ref.isEmpty()){gap(box,4);box.addView(text("Reference: "+ref,12,muted,false));}
                gap(box,10);
                Button resolve=button(status.equals("Resolved")?"Mark as Open":"Mark as Resolved",()->{
                    db.updateStatus(id,status.equals("Resolved")?"Open":"Resolved"); showComplaints();
                });
                box.addView(resolve,new LinearLayout.LayoutParams(-1, dp(46))); card(content,box);
            }
            c.close();
        }
        gap(content,8); content.addView(button("+ Register another complaint",this::showNewComplaint),new LinearLayout.LayoutParams(-1, dp(52)));
    }
    private void showProfile() {
        setup("My Profile", true);
        LinearLayout info=column(); info.addView(text("ACCOUNT DETAILS",12,blue,true)); gap(info,12);
        info.addView(text("Full name",12,muted,false)); info.addView(text(currentName,18,ink,true)); gap(info,12);
        info.addView(text("Email address",12,muted,false)); info.addView(text(currentEmail,16,ink,true)); gap(info,12);
        info.addView(text("Complaints registered",12,muted,false)); info.addView(text(String.valueOf(db.count(currentEmail)),18,ink,true));
        card(content,info); gap(content,8);
        content.addView(button("Logout",()->new AlertDialog.Builder(this).setTitle("Logout").setMessage("Are you sure you want to logout?")
            .setNegativeButton("Cancel",null).setPositiveButton("Logout",(d,w)->{currentEmail="";currentName="";showWelcome();}).show()),new LinearLayout.LayoutParams(-1, dp(52)));
        gap(content,16);
        TextView note=text("Customer Complaint Management System\nDemo version: data is stored locally on this device. This app does not connect to a server.",13,muted,false);
        note.setPadding(dp(4),dp(8),dp(4),dp(8)); content.addView(note);
    }
    private void toast(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }

    private static class DB extends SQLiteOpenHelper {
        DB(Activity a){super(a,"complaint_manager.db",null,1);}
        public void onCreate(SQLiteDatabase d){
            d.execSQL("CREATE TABLE users(email TEXT PRIMARY KEY,name TEXT NOT NULL,password TEXT NOT NULL,phone TEXT)");
            d.execSQL("CREATE TABLE complaints(id TEXT PRIMARY KEY,email TEXT NOT NULL,subject TEXT NOT NULL,category TEXT,reference TEXT,details TEXT,status TEXT,date TEXT)");
        }
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV){}
        boolean register(String n,String e,String p,String phone){
            SQLiteDatabase d=getWritableDatabase(); ContentValues v=new ContentValues();v.put("email",e);v.put("name",n);v.put("password",p);v.put("phone",phone);
            try{d.insertOrThrow("users",null,v);return true;}catch(Exception ex){return false;}
        }
        String[] login(String e,String p){
            Cursor c=getReadableDatabase().rawQuery("SELECT name FROM users WHERE email=? AND password=?",new String[]{e,p});
            String[] result=null;if(c.moveToFirst())result=new String[]{c.getString(0)};c.close();return result;
        }
        String addComplaint(String e,String s,String cat,String ref,String details){
            String id="CMP-"+new SimpleDateFormat("yyMMddHHmmss",Locale.US).format(new Date());
            ContentValues v=new ContentValues();v.put("id",id);v.put("email",e);v.put("subject",s);v.put("category",cat);v.put("reference",ref);v.put("details",details);v.put("status","Open");v.put("date",new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new Date()));
            getWritableDatabase().insert("complaints",null,v);return id;
        }
        Cursor complaints(String e){return getReadableDatabase().rawQuery("SELECT id,subject,category,status,date,details,reference FROM complaints WHERE email=? ORDER BY rowid DESC",new String[]{e});}
        int count(String e){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM complaints WHERE email=?",new String[]{e});c.moveToFirst();int n=c.getInt(0);c.close();return n;}
        int countOpen(String e){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM complaints WHERE email=? AND status='Open'",new String[]{e});c.moveToFirst();int n=c.getInt(0);c.close();return n;}
        void updateStatus(String id,String s){ContentValues v=new ContentValues();v.put("status",s);getWritableDatabase().update("complaints",v,"id=?",new String[]{id});}
    }
}
