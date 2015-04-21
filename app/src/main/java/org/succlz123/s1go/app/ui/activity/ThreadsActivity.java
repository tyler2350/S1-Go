package org.succlz123.s1go.app.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.melnykov.fab.FloatingActionButton;
import org.succlz123.s1go.app.R;
import org.succlz123.s1go.app.S1GoApplication;
import org.succlz123.s1go.app.bean.forum.ForumForumThreadlist;
import org.succlz123.s1go.app.bean.forum.ForumObject;
import org.succlz123.s1go.app.dao.Api.ForumTitleApi;
import org.succlz123.s1go.app.dao.Helper.S1FidHelper;
import org.succlz123.s1go.app.support.swingindicator.SwingIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fashi on 2015/4/14.
 */
public class ThreadsActivity extends ActionBarActivity {
	private ListView mListView;
	private String mFid;
	private ForumObject forumObject;
	private AppAdapet mApdater;
	private Toolbar mToolbar;
	private Boolean isLogin;
	private String ToolbarTitle;
	private List<ForumForumThreadlist> mForumForumThreadlist;
	private SwingIndicator mSwingIndicator;
	private FloatingActionButton mFloatingActionButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.threads_activity);
		mFid = getIntent().getStringExtra("fid");
		initViews();
		setToolbar();
		setFloatingActionButton();
		new GetThreadsTitleAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(ThreadsActivity.this, ReviewsActivity.class);
				intent.putExtra("tid", mForumForumThreadlist.get(position).getTid());
				intent.putExtra("title", mForumForumThreadlist.get(position).getSubject());
				startActivity(intent);
			}
		});
	}

	private void initViews() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mSwingIndicator = (SwingIndicator) findViewById(R.id.threads_progress);
		mFloatingActionButton = (FloatingActionButton) findViewById(R.id.thread_fab);
		mListView = (ListView) findViewById(R.id.threads_base_activity_listview);
	}

	private void setToolbar() {
		ToolbarTitle = S1FidHelper.GetS1Fid(Integer.valueOf(mFid));
		mToolbar.setTitle(ToolbarTitle);
		mToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
		mToolbar.setSubtitleTextColor(Color.parseColor("#ffffff"));
		mToolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitle);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void setFloatingActionButton() {
		mFloatingActionButton.setShadow(true);
		mFloatingActionButton.setType(FloatingActionButton.TYPE_NORMAL);
		mFloatingActionButton.setColorNormal(getResources().getColor(R.color.base));
		mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ThreadsActivity.this, SetThreadsActivity.class);
				startActivityForResult(intent, 1);
			}
		});
		mFloatingActionButton.attachToListView(mListView);//把listview和浮动imagebutton组合
	}

	private class AppAdapet extends BaseAdapter {

		private class ViewHolder {
			private TextView title;
			private TextView name;
			private TextView time;
			private TextView lastTime;
			private TextView lastPoster;
			private TextView reply;
			private TextView click;
			private TextView fid;
		}

		@Override
		public int getCount() {
			if (forumObject != null) {
				return forumObject.getVariables().getForum_threadlist().size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.threads_listview_item, parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.threads_listview_title);
				holder.name = (TextView) convertView.findViewById(R.id.threads_listview_name);
				holder.time = (TextView) convertView.findViewById(R.id.threads_listview_time);
				holder.lastTime = (TextView) convertView.findViewById(R.id.threads_listview_last_post_time);
				holder.lastPoster = (TextView) convertView.findViewById(R.id.threads_listview_last_poster);
				holder.reply = (TextView) convertView.findViewById(R.id.threads_listview_reply);
				holder.click = (TextView) convertView.findViewById(R.id.threads_listview_click);
				holder.fid = (TextView) convertView.findViewById(R.id.threads_listview_fid);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			mForumForumThreadlist = new ArrayList<ForumForumThreadlist>();
			mForumForumThreadlist = forumObject.getVariables().getForum_threadlist();
			holder.title.setText(mForumForumThreadlist.get(position).getSubject());
			holder.name.setText(mForumForumThreadlist.get(position).getAuthor());
			holder.time.setText(Html.fromHtml(mForumForumThreadlist.get(position).getDateline()));
			holder.lastTime.setText(Html.fromHtml(mForumForumThreadlist.get(position).getLastpost()));
			holder.lastPoster.setText(mForumForumThreadlist.get(position).getLastposter());
			holder.reply.setText(mForumForumThreadlist.get(position).getReplies());
			holder.click.setText(mForumForumThreadlist.get(position).getViews());
			holder.fid.setText(null);

			return convertView;
		}
	}

	private class GetThreadsTitleAsyncTask extends AsyncTask<Void, Void, ForumObject> {

		private HashMap<String, String> hearders = new HashMap<String, String>();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (S1GoApplication.getInstance().getUserInfo() == null) {
			} else {
				String cookie = S1GoApplication.getInstance().getUserInfo().getCookiepre();
				String auth = "auth=" + Uri.encode(S1GoApplication.getInstance().getUserInfo().getAuth());
				String saltkey = "saltkey=" + S1GoApplication.getInstance().getUserInfo().getSaltkey();
				this.hearders.put("Cookie", cookie + auth + ";" + cookie + saltkey + ";");
			}
		}

		@Override
		protected ForumObject doInBackground(Void... params) {
			return ForumTitleApi.getForumTitle(mFid, hearders);
		}

		@Override
		protected void onPostExecute(ForumObject aVoid) {
			super.onPostExecute(aVoid);
			forumObject = aVoid;
			isLogin = (forumObject != null && forumObject.getMessage() == null);
			if (!isLogin) {
				Toast.makeText(ThreadsActivity.this, "抱歉，您尚未登录，没有权限访问该版块", Toast.LENGTH_LONG).show();
			} else if (isLogin) {
				mApdater = new AppAdapet();
				mListView.setAdapter(mApdater);
				mSwingIndicator.setVisibility(View.GONE);
				mFloatingActionButton.setVisibility(View.VISIBLE);
				mApdater.notifyDataSetChanged();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}