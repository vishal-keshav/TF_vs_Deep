package com.bulletcross.vishal.tf

import android.content.Context
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Xml
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bulletcross.vishal.tf.views.data_drawer
import com.bulletcross.vishal.tf.views.data_recorder

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener,  View.OnTouchListener{
    //Layout elements
    var button_benchmark:Button? = null
    var button_clear:Button? = null
    var button_predict:Button? = null
    var text_benchmark: TextView? = null
    var text_accuracy: TextView? = null
    //var text_predict: TextView? = null
    var text_input:EditText? = null
    //Listeners
    var data_rec: data_recorder? = null
    var data_draw: data_drawer? = null

    var temp_x:Float = 0.0F
    var temp_y:Float = 0.0F
    var temp_point = PointF()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Obtain by ID
        button_benchmark = findViewById(R.id.button)
        button_clear = findViewById(R.id.button3)
        button_predict = findViewById(R.id.button2)
        text_benchmark = findViewById(R.id.textView2)
        text_accuracy = findViewById(R.id.textView4)
        //text_predict = findViewById
        text_input = findViewById(R.id.editText)

        data_draw = findViewById(R.id.draw)
        data_rec = data_recorder()

        data_draw?.set_data(data_rec!!)
        data_draw?.setOnTouchListener(this)
        button_benchmark?.setOnClickListener(this)
        button_clear?.setOnClickListener(this)
        button_predict?.setOnClickListener(this)

    }

    override fun onResume(){
        data_draw?.onResume()
        super.onResume()
    }

    override fun onPause(){
        data_draw?.onPause()
        super.onPause()
    }

    override fun onClick(v:View){
        if(v.getId() == R.id.button){
            //Run Benchmark

        }
        else if(v.getId() == R.id.button3){
            //Clear data_draw view
            var outputStream: FileOutputStream
            var sb:StringBuilder = StringBuilder()
            var width:Int = data_draw?.bitmap!!.width
            var height:Int = data_draw?.bitmap!!.height
            var root_path:String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            var mydir: File = File(root_path + "/gesture_data/")
            println(mydir)
            if(!mydir.exists()){
                mydir.mkdirs()
                println("Directory created")
            }
            try{
                var my_file:File = File(mydir.path + "/data_file.csv")
                outputStream = openFileOutput(my_file.getPath(), Context.MODE_APPEND)
                var pixel_int:IntArray = IntArray(width*height)
                data_draw?.bitmap!!.getPixels(pixel_int, 0, width, 0, 0, width, height)
                var pixel_float:Float
                for (i in 0..pixel_int.size-1){
                    var p_int:Int = pixel_int[i]
                    var p_standard:Int = p_int and 0xff
                    pixel_float = (0xff - p_standard).toFloat()/255.0F
                    sb.append(pixel_float.toString())
                    sb.append(",")
                }
                sb.append(text_input?.text.toString())
                outputStream.write(sb.toString().toByteArray())
                println(mydir.path + this.getFilesDir().getAbsolutePath())
                outputStream.close()

            } catch (e:Exception){
                println("Exception!!")
            }

            data_rec?.clear_lines()
            data_draw?.reset()
            data_draw?.invalidate() //Force redraw on screen
            text_accuracy?.setText("C")
        }
        else if(v.getId() == R.id.button2){
            //Run prediction model on view data
        }
    }

    override fun onTouch(v:View, event: MotionEvent):Boolean{
        var action = event.action
        if(action == MotionEvent.ACTION_DOWN){
            temp_x = event.getX()
            temp_y = event.getY()
            data_draw?.map_data_points(temp_x, temp_y, temp_point)
            data_rec?.record_touch(temp_point.x, temp_point.y)
            text_accuracy?.setText("D")
            return true
        }
        else if(action == MotionEvent.ACTION_MOVE){
            var x:Float = event.getX()
            var y:Float = event.getY()
            data_draw?.map_data_points(temp_x, temp_y, temp_point)
            data_rec?.record_motion(temp_point.x, temp_point.y)
            temp_x = x
            temp_y = y
            text_accuracy?.setText("M")
            data_draw?.invalidate()
            return true
        }
        else if(action == MotionEvent.ACTION_UP){
            data_rec?.finalize_motion()
            text_accuracy?.setText("U")
            return true
        }
        return false
    }

}
