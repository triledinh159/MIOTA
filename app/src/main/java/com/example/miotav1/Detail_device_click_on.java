package com.example.miotav1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;

public class Detail_device_click_on extends AppCompatActivity {
    private LineChart mChart;
    private TextView tvName, tvDevice, tvTopic;
    private EditText etTopic;
    private Button btnEditTopic, btnSaveTopic;
    private boolean isChartVisible = true;
    private static final float TOTAL_MEMORY = 16.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_device_click_on);

        // Ánh xạ các thành phần giao diện
        tvName = findViewById(R.id.detail_name);
        tvDevice = findViewById(R.id.detail_device);
        tvTopic = findViewById(R.id.detail_topic);
        etTopic = findViewById(R.id.editTextTopic);
        btnEditTopic = findViewById(R.id.btnEditTopic);
        btnSaveTopic = findViewById(R.id.btnSaveTopic);
        // Nhận dữ liệu từ Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("name");
            String device = extras.getString("device");
            String topic = extras.getString("topic");
            String statistic = extras.getString("statistic");
            // Hiển thị thông tin lên giao diện
            tvName.setText(name);
            tvDevice.setText(device);
            tvTopic.setText(topic);
            etTopic.setText(topic);  // Thiết lập giá trị mặc định cho EditText

            // Ẩn EditText khi chưa ấn nút sửa
            etTopic.setVisibility(View.GONE);
            btnEditTopic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Hiển thị EditText và ẩn TextView
                    etTopic.setVisibility(View.VISIBLE);
                    tvTopic.setVisibility(View.GONE);

                    // Hiển thị nút lưu và ẩn nút sửa
                    btnEditTopic.setVisibility(View.GONE);
                    btnSaveTopic.setVisibility(View.VISIBLE);
                }
            });

            // Xử lý sự kiện khi ấn nút lưu
            // Khi người dùng lưu lại thông tin
            btnSaveTopic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Lấy giá trị mới từ EditText
                    String newTopic = etTopic.getText().toString();

                    // Cập nhật giá trị trên TextView và ẩn EditText
                    tvTopic.setText(newTopic);
                    etTopic.setVisibility(View.GONE);
                    tvTopic.setVisibility(View.VISIBLE);

                    // Hiển thị lại nút sửa và ẩn nút lưu
                    btnEditTopic.setVisibility(View.VISIBLE);
                    btnSaveTopic.setVisibility(View.GONE);

                    // Trả về kết quả cho Home activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newTopic", newTopic);
                    setResult(RESULT_OK, resultIntent);

                    // TODO: Cập nhật giá trị mới vào danh sách hoặc lưu vào database nếu cần

                    // Đóng Detail_device_click_on activity
                }
            });
            mChart = (LineChart) findViewById(R.id.chart);

            setupChart();
            setupAxes();
            setupData();
            setLegend();
            findViewById(R.id.btnToggleChart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleChartVisibility();
                }
            });

        }
    }
    private void toggleChartVisibility() {
        if (isChartVisible) {
            // If chart is currently visible, hide it
            mChart.setVisibility(View.GONE);
        } else {
            // If chart is currently hidden, show it
            mChart.setVisibility(View.VISIBLE);
        }
        isChartVisible = !isChartVisible;
    };
    private void setupChart() {
        // disable description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // enable scaling
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        // set an alternative background color
        mChart.setBackgroundColor(Color.DKGRAY);
    }
    private void setupAxes() {
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(TOTAL_MEMORY);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Add a limit line
//        LimitLine ll = new LimitLine(LIMIT_MAX_MEMORY, "Upper Limit");
//        ll.setLineWidth(2f);
//        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//        ll.setTextSize(10f);
//        ll.setTextColor(Color.WHITE);
//        // reset all limit lines to avoid overlapping lines
//        leftAxis.removeAllLimitLines();
//        leftAxis.addLimitLine(ll);
//        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(true);
    }
    private void setupData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
    }

    private void setLegend() {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(Color.WHITE);
    }
}
