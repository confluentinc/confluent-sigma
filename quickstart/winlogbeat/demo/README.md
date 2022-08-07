# Create Demo Environment
## Configure AWS VPC and EC2 Hosts
1. Clone the repository:
    ```
    git clone https://github.com/confluentinc/cyber
    ````
1. Change to the demo directory
    ```
    cd cyber-master/quickstart/winlogbeat/demo
    ```
1. Edit the `yakshaving.conf` config file 
    - Change the following fields
        - `vm_name`
        - `Owner_Name`
        - `your_pem`
        - `your_email`
    - Change the following fields according to your Confluent Cloud client settings or local Confluent Platform client settings:
        - `bootstrap.servers`
        - `sasl.username`
        - `sasl.password`

2. Create the AWS VPC, Subnet, Security Gateway, and Infinidash
    ```
    python3 create_demo_environment.py
    ```

5. Create the traffic mirror target and session
    ```
    python3 create_mirror_session.py
    ```
6. You should now be able to SSH to the Zeek host and sniff traffic from the Windows host on `eth0` (omitting SSH traffic)
    ```
    sudo tcpdump -n -i eth0 not port 22
    ```
    Note: seeing VXLAN UDP packets to port 4789 indicates success!  This is how AWS sends mirrored traffic from a source to destination:
    ```
    01:12:27.878360 IP 10.100.0.193.65448 > 10.100.0.106.4789: VXLAN, flags [I] (0x08), vni 5131571
    ```
    Thankfully, Zeek will decode this automatically for us!

## Configure Zeek
### Edit config files
1. Either clone this repository to work locally and then upload files, or SSH to the Zeek system edit files on the host itself. (the `send-to-confluent-cloud.zeek` file does not yet exist on the running AMI)
1. To clone the Cyber Quick start repository locally:
    ```
    git clone https://github.com/confluentinc/cyber
    ```

2. Edit the `node.cfg` file to change the interface setting:
    ```
    [zeek]
    type=standalone
    host=localhost
    interface=eth0
    ```
3. Edit the last few lines of the `/usr/loca/zeek/share/zeek/site/local.zeek` file to send data to Confluent Cloud:
    ```
    ...
    @load policy/tuning/json-logs

    @load send-to-confluent-cloud
    #@load send-to-local-kafka
    ``` 
    Or to send to a local instance of Confluent
    ```
    ...
    @load policy/tuning/json-logs

    #@load send-to-confluent-cloud
    @load send-to-local-kafka
    ```

4. Edit the `send-to-confluent-cloud.zeek` file to include your API Key (`sasl.username`), API Secret (`sasl.password`) and `metadata.broker.list`

5. Copy the newly edited files to their appropriate locations on the Zeek streamer:
    ```
    /usr/local/zeek/etc/node.cfg
    /usr/local/zeek/share/zeek/site/send-to-confluent-cloud.zeek
    /usr/local/zeek/share/zeek/site/local.zeek
    ```

6. Start Zeek:
    ```
    sudo /usr/local/zeek/bin/zeekctl deploy
    ```
7. Possibility this doesn't work on a Confluent Cloud deployment that does not already have Zeek topics created.
9. Check Confluent Cloud for Zeek topics, e.g. `conn`, `dns`, `ssl`, etc.

## Install Sysmon
1. Connect as Administrator to the Windows EC2 host via RDP
2. Open a PowerShell terminal
3. Copy/paste the `instally_sysmon.ps1` PowerShell script into the terinal and press enter
4. Sysmon is now running.

## Install/Configure Winlogbeat
1. While still connected via RDP to the Windows EC2 host..
2. PowerShell terminal is still open, right?
3. Copy/paste the `get_winlogbeat.ps1` file into the terminal and press enter
4. Stretch - this takes a few minutes
5. On the Windows system, edit the `winlogbeat.yml` config file in `c:\Program Files\Winlogbeat\`
    - Under the Confluent section - Change values for the following fields:
        - `hosts:`
        - `topic:`
        - `username:`
        - `password:`

6. Start Winlogbeat:
    ```
    cd 'C:\Program Files\Winlogbeat'
    .\install-service-winlogbeat.ps1
    ```
7. Open the Services app, make sure Winlogbeat is running - restart if it it's not.



