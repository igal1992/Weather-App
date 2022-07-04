package activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import xd.activities.R
import xd.activities.databinding.ActivityLoginBinding
import xd.activities.databinding.ActivityRegisterBinding
import xd.activities.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var topAnimation: Animation
    private lateinit var bottomAnimation: Animation
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var mainIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        topAnimation = AnimationUtils.loadAnimation(this,R.anim.top_animation)
        bottomAnimation = AnimationUtils.loadAnimation(this,R.anim.bottom_animation)
        imageView = binding.imageView
        textView = binding.text

        //start the animation
        imageView.startAnimation(topAnimation)
        textView.startAnimation(bottomAnimation)


        Handler().postDelayed({
            mainIntent = Intent(this, Login::class.java)
            startActivity(mainIntent)
            finish()
        },1500)
    }
}