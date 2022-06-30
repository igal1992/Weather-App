package activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import xd.activities.R
import xd.activities.databinding.ActivityLoginBinding
import xd.activities.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {

    private lateinit var  binding: ActivityRegisterBinding
    private lateinit var  firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        //get an instance from the db
        firebaseAuth = FirebaseAuth.getInstance()

        //create a listener for the login now button to move to the login activity
        binding.loginNowButton.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        //create the listener for the register button
        binding.registerButton.setOnClickListener{
            this.register()
        }
    }

     private fun register(){
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val passwordRep = binding.passwordRep.text.toString()

        //check if all fields are filled
        if(email.isNotEmpty() && password.isNotEmpty() && passwordRep.isNotEmpty()){
            //check if both fields are equal
            if(password == passwordRep) {
                //create a user with username field and password field
                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                    if(it.isSuccessful){//if successfully created switch activity to login screen
                        val intent = Intent(this, activities.Login::class.java)
                        startActivity(intent)
                    }else{//else make an error message
                        Toast.makeText(this,it.exception.toString() ,Toast.LENGTH_SHORT).show()
                    }
                }
            }else{//else make an error message
                Toast.makeText(this,"Password is not matching",Toast.LENGTH_SHORT).show()
            }
        }else{//else make an error message
            Toast.makeText(this,"Empty Fields Are not Allowed !!",Toast.LENGTH_SHORT).show()
        }

    }
}