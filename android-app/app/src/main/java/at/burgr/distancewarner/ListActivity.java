// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package at.burgr.distancewarner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

import at.burgr.distancewarner.data.AppDatabase;
import at.burgr.distancewarner.data.Warning;
import at.burgr.distancewarner.data.WarningDao;

/**
 * An activity that displays a list of stored warnings
 */
public class ListActivity extends BaseActivity {
    private WarningDao warningDao;
    ListView warningList;

    @Override
    void getCreateInActivity(Bundle savedInstanceState) {
        setContentView(R.layout.activity_list);
        warningList = (ListView)findViewById(R.id.warningListView);

        warningDao = AppDatabase.getInstance(this).warningDao();

        getNewContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.list_menu_delete_all:
                warningDao.deleteAll();
                getNewContent();
                return true;
            case R.id.list_menu_refresh:
                getNewContent();
                return true;
            default:
                return false;
        }
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_list;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_list;
    }


    private void getNewContent() {
        final List<Warning> all = warningDao.getAll();

        ArrayAdapter<Warning> arrayAdapter = new ListViewAdapter(this, R.layout.warning_list, all);
        warningList.setAdapter(arrayAdapter);

        warningList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), all.get(position).toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
