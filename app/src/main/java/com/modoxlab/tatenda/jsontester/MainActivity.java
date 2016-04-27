package com.modoxlab.tatenda.jsontester;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView lvMovie;
    private ProgressBar progressBar;
    private URL url;
    private HttpURLConnection connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.modoxlab.tatenda.jsontester.R.layout.activity_main);

        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config); // Do it on Application start

        lvMovie = (ListView) findViewById(com.modoxlab.tatenda.jsontester.R.id.lvMovie);
        progressBar = (ProgressBar) findViewById(com.modoxlab.tatenda.jsontester.R.id.pbload);
        new Sync().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.modoxlab.tatenda.jsontester.R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.modoxlab.tatenda.jsontester.R.id.asrefresh) {
            new Sync().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class Sync extends AsyncTask<Void, Void, List<MovieModel>> {
        BufferedReader br = null;
        private String temp, jsondata;

        @Override
        protected List<MovieModel> doInBackground(Void... params) {
            try {
                url = new URL("http://jsonparsing.parseapp.com/jsonData/moviesData.txt");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer sb = new StringBuffer();
                while ((temp = br.readLine()) != null) {
                    sb.append(temp);
                }
                jsondata = sb.toString();
                JSONObject movieObject = new JSONObject(jsondata);
                JSONArray movieArray = movieObject.getJSONArray("movies");
                List<MovieModel> movieModelList = new ArrayList<>();
                Gson gson = new Gson();
                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject jsonObject = movieArray.getJSONObject(i);
                    MovieModel movieModel = gson.fromJson(jsonObject.toString(),MovieModel.class);
                    movieModelList.add(movieModel);
                }
                return movieModelList;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            lvMovie.setAdapter(null);
        }

        @Override
        protected void onPostExecute(List<MovieModel> movieModels) {
            progressBar.setVisibility(View.GONE);
            if (movieModels == null)
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection!!", Toast.LENGTH_SHORT).show();
            else {
                Adapter adapter = new Adapter(getApplicationContext(), com.modoxlab.tatenda.jsontester.R.layout.adapter_layout, movieModels);
                lvMovie.setAdapter(adapter);
            }
        }
    }

    private class Adapter extends ArrayAdapter {

        private List<MovieModel> movieModelList;
        private int resource;
        private LayoutInflater layoutInflater;

        public Adapter(Context context, int resource, List objects) {
            super(context, resource, objects);
            this.resource = resource;
            movieModelList = objects;
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                holder = new Holder();
                convertView = layoutInflater.inflate(resource, null);
                holder.imageView = (ImageView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.ivmovie);
                holder.tvMovie = (TextView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.tvHeading);
                holder.tvCast = (TextView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.tvCast);
                holder.tvDirector = (TextView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.tvDirector);
                holder.tvDuration = (TextView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.tvDuration);
                holder.tvYear = (TextView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.tvYear);
                holder.ratingBar = (RatingBar) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.rbMovie);
                holder.tvStory = (TextView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.tvStory);
                holder.tvTagline = (TextView) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.tvTagline);
                holder.progressBar = (ProgressBar) convertView.findViewById(com.modoxlab.tatenda.jsontester.R.id.pbImage);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.tvMovie.setText(movieModelList.get(position).getMovie());
            holder.tvDirector.setText("Director: " + movieModelList.get(position).getDirector());
            holder.tvDuration.setText("Length: " + movieModelList.get(position).getDuration());
            holder.tvYear.setText("Year: " + movieModelList.get(position).getYear());
            holder.tvStory.setText(movieModelList.get(position).getSotry());
            holder.tvTagline.setText(movieModelList.get(position).getTagline());
            holder.ratingBar.setRating(movieModelList.get(position).getRating() / 2);
            StringBuffer stringBuffer = new StringBuffer();
            for (MovieModel.Cast cast : movieModelList.get(position).getCast())
                stringBuffer.append(cast.getName() + " , ");
            stringBuffer.delete(stringBuffer.lastIndexOf(","), stringBuffer.length());
            holder.tvCast.setText("Cast: " + stringBuffer);

            // Then later, when you want to display image
            final Holder finalHolder = holder;
            ImageLoader.getInstance().displayImage(movieModelList.get(position).getImage(), holder.imageView, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    finalHolder.progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Toast.makeText(getApplicationContext(), "Failed To Load Image. Error: " + failReason, Toast.LENGTH_LONG).show();
                    finalHolder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    finalHolder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    finalHolder.progressBar.setVisibility(View.GONE);
                }
            }); // Default options will be used
            return convertView;
        }

        private class Holder {
            private ImageView imageView;
            private TextView tvMovie;
            private TextView tvCast;
            private TextView tvDirector;
            private TextView tvDuration;
            private TextView tvYear;
            private RatingBar ratingBar;
            private TextView tvStory;
            private TextView tvTagline;
            private ProgressBar progressBar;
        }
    }
}
