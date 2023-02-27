package com.mka.ipcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //Define variable type
    EditText ipText, subnetText;
    TextView resultText;
    Button calculateButton, resetButton, aboutButton, helpButton;

    //Assign variable with view elements
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipText = findViewById(R.id.ip_edit_text);
        subnetText = findViewById(R.id.subnet_edit_text);
        resultText = findViewById(R.id.result_text_view);
        calculateButton = findViewById(R.id.calculate_button);
        resetButton = findViewById(R.id.reset_button);
        aboutButton = findViewById(R.id.about_button);
        helpButton = findViewById(R.id.help_button);

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });


    //Calculate Button function
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipText.getText().toString().trim();
                String subnet = subnetText.getText().toString().trim();
                //if IP and subnet mask is valid, trim and assign to into array for calculation
                if (isValidIp(ip) && isValidSubnet(subnet)) {
                    int[] ipArr = toIntArray(ip);
                    int[] subnetArr = toIntArray(subnet);
                    int[] netmaskArr = calculateNetmask(subnetArr);
                    int[] networkArr = calculateNetwork(ipArr, netmaskArr);
                    int[] wildcardArr = calculateWildcard(netmaskArr);
                    int maxHosts = calculateMaxHosts(subnetArr);
                    int minHosts = calculateMinHosts(subnetArr);
                    //Show result
                    String result = "Class:     " + getIpClass(ipArr) +
                            "\nIP Range:    " + (toIpString(networkArr)) + " - " + toIpString(calculateBroadcast(networkArr, wildcardArr)) +
                            "\nMax Hosts:   " + maxHosts +
                            "\nMin Hosts:   " + minHosts +
                            "\nBroadcast Address:     " + toIpString(calculateBroadcast(networkArr, wildcardArr)) +
                            "\nNetwork Address:     " + toIpString(networkArr) +
                            "\nWildcard Address:    " + toIpString(wildcardArr);

                    resultText.setText(result);
                } else {
                    //return error to user if invalid ip or subnet is inputted
                    resultText.setText("Invalid IP or subnet mask.");
                }
            }
        });

        //rest input and result's view
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipText.setText("");
                subnetText.setText("");
                ipText.setHint("IP Address (e.g. 192.168.0.1)");
                subnetText.setHint("Subnet Mask (e.g. 255.255.255.0)");
                resultText.setText("Result Will Be Show Here");
            }
        });
    }

    //IP verify
    private boolean isValidIp(String ip) {
        //find split part with regex
        String[] parts = ip.split("\\.");
        //IP verify logic
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            int num = Integer.parseInt(part);

            if (num < 0 || num > 255) {
                return false;
            }
        }
        return true;
    }

    //Subnet verify
    private boolean isValidSubnet(String subnet) {
        //find split part with regex
        String[] parts = subnet.split("\\.");

        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            int num = Integer.parseInt(part);

            if (num < 0 || num > 255) {
                return false;
            }
        }
        return true;
    }

    //Create array[4] for IP and Subnet Mask
    private int[] toIntArray(String ip) {
        String[] parts = ip.split("\\.");
        int[] result = new int[4];

        for (int i = 0; i < 4; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }

    //Append IP and subnet array back to string for result view
    private String toIpString(int[] ipArr) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            sb.append(ipArr[i]);

            if (i < 3) {
                sb.append(".");
            }
        }

        return sb.toString();
    }

    //Find the Class of IP and Subnet
    private String getIpClass(int[] ipArr) {
        if (ipArr[0] >= 1 && ipArr[0] <= 126) {
            return "A";
        } else if (ipArr[0] >= 128 && ipArr[0] <= 191) {
            return "B";
        } else if (ipArr[0] >= 192 && ipArr[0] <= 223) {
            return "C";
        } else if (ipArr[0] >= 224 && ipArr[0] <= 239) {
            return "D";
        } else {
            return "E";
        }
    }

    //Find the netmask with the subnet
    private int[] calculateNetmask(int[] subnetArr) {
        int[] result = new int[4];
        int mask = 0;

        for (int i = 0; i < subnetArr.length; i++) {
            mask |= (subnetArr[i] << (24 - 8 * i));
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = (mask >> (24 - 8 * i)) & 255;
        }
        return result;
    }

    //Find the network address
    private int[] calculateNetwork(int[] ipArr, int[] netmaskArr) {
        int[] result = new int[4];

        for (int i = 0; i < result.length; i++) {
            result[i] = ipArr[i] & netmaskArr[i];
        }
        return result;
    }

    //revert the subnet mask into wildcard address
    private int[] calculateWildcard(int[] netmaskArr) {
        int[] result = new int[4];

        for (int i = 0; i < result.length; i++) {
            result[i] = 255 - netmaskArr[i];
        }
        return result;
    }

    //Calculate the maximum hosts allow in this network
    private int calculateMaxHosts(int[] subnetArr) {
        int mask = 0;

        for (int i = 0; i < subnetArr.length; i++) {
            mask |= (subnetArr[i] << (24 - 8 * i));
        }
        return 32 - Integer.bitCount(mask);
    }

    //Calculate the minimum hosts require in this network
    private int calculateMinHosts(int[] subnetArr) {
        return (int) Math.pow(2, calculateMaxHosts(subnetArr)) - 2;
    }

    //Calculate the broadcast address of this network
    private int[] calculateBroadcast(int[] networkArr, int[] wildcardArr) {
        int[] result = new int[4];

        for (int i = 0; i < result.length; i++) {
            result[i] = networkArr[i] | wildcardArr[i];
        }
        return result;
    }
}
