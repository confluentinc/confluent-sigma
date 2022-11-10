#!/usr/local/bin/python3

import boto3
from configparser import ConfigParser
import argparse
import json


parser = argparse.ArgumentParser(description=
    '''This script creates a VPC Mirror Session in AWS''')
parser.add_argument('-f', dest='config_file', action='store', help='the full path of the config file')
parser.add_argument('-d', dest='debug', action='store_true', help='turn on debugging output')


args = parser.parse_args()

def parse_configs():
    if args.config_file:
        config_file = args.config_file
    else:
        config_file = 'yak_shaving.conf'
    cfg = ConfigParser()
    cfg.read(config_file)
    figs = {}
    figs['pem'] = cfg.get('zeek', 'your_pem')
    figs['demo_name'] = cfg.get('demo', 'demo_name')
    figs['Owner_Email'] = cfg.get('zeek', 'your_email')
    figs['Owner_Name'] = cfg.get('zeek', 'Owner_Name')
    return(figs)

def make_tags(name_tag):
    myfigs = parse_configs()
    demo_name = myfigs['demo_name']
    Owner_Email = myfigs['Owner_Email']
    Owner_Name = myfigs['Owner_Name']
    tags = [
    {
        'Key': 'Name',
        'Value': name_tag
    },
    {
        'Key': 'Owner_Name',
        'Value': Owner_Name
    },
    {
        'Key': 'Owner_Email',
        'Value': Owner_Email
    },
    {
        'Key': 'demo',
        'Value': demo_name
    }
    ]
    return(tags)

def get_running_instance_info(demo_host):
    myfigs = parse_configs()
    demo_name = myfigs['demo_name']
    pem = myfigs['pem']
    Owner_Email = myfigs['Owner_Email']
    Owner_Name = myfigs['Owner_Name']
    ec2 = boto3.client('ec2')
    node_filters = [
        {'Name': 'tag:demo', 'Values': [demo_name]},
        {'Name': 'key-name', 'Values': [pem]},
        {'Name': 'tag:Owner_Email', 'Values': [Owner_Email]},
        {'Name': 'tag:Owner_Name', 'Values': [Owner_Name]},
        {'Name': 'instance-state-name', 'Values': ['running']},
        {'Name': 'tag:demo_host', 'Values': [demo_host]}
    ]
    response = ec2.describe_instances(Filters=node_filters)
    return(response)


def parse_instance_info(response):
    myfigs = parse_configs()
    demo_name = myfigs['demo_name']
    Reservations = response['Reservations']

    for res_dict in Reservations:
        instance_list = res_dict['Instances']
        for instance_dict in instance_list:
            try:
                instance_dict['PrivateIpAddress']
            except KeyError:
                private_ip = "NULL"
            else:
                private_ip = instance_dict['PrivateIpAddress']
            try:
                instance_dict['PrivateDnsName']
            except KeyError:
                private_dns = "NULL"
            else:
                private_dns = instance_dict['PrivateDnsName']
            try:
                instance_dict['PublicDnsName']
            except KeyError:
                public_dns = "NULL"
            else:
                public_dns = instance_dict['PublicDnsName']
            try:
                instance_dict['PublicIpAddress']
            except KeyError:
                public_ip = "NULL"
            else:
                public_ip = instance_dict['PublicIpAddress']
            try:
                NetworkInterfaces = instance_dict['NetworkInterfaces']
                first_interface = NetworkInterfaces[0]
                NetworkInterfaceId = first_interface['NetworkInterfaceId']
            except KeyError:
                NetworkInterfaceId = "NULL"
            else:
                NetworkInterfaces = instance_dict['NetworkInterfaces']
                first_interface = NetworkInterfaces[0]
                NetworkInterfaceId = first_interface['NetworkInterfaceId']     
                    
            tags = instance_dict['Tags']
            for tag_dict in tags:
                for key in tag_dict:
                    if tag_dict.get(key) == 'Name':
                        ec2_name = tag_dict['Value']
                    elif tag_dict.get(key) == 'demo_name':
                        demo_name = tag_dict['Value']

        host_info = {
            'ec2_name': ec2_name,
            'demo_name': demo_name,
            'public_ip': public_ip,
            'private_ip': private_ip,
            'public_dns': public_dns,
            'private_dns': private_dns,
            'NetworkInterfaceId': NetworkInterfaceId
            }
        return(host_info)


def create_traffic_mirror_target(zeek_nic_id):
    myfigs = parse_configs()
    Owner_Name = myfigs['Owner_Name']
    Owner_Email = myfigs['Owner_Email']
    demo_name = myfigs['demo_name']
    ec2 = boto3.client('ec2')
    response = ec2.create_traffic_mirror_target(
        NetworkInterfaceId = zeek_nic_id,
        Description = 'bhayes traffic mirror target for winlogbeat demo',
        TagSpecifications=[
            {
                'ResourceType': 'traffic-mirror-target',
                'Tags': make_tags('winlogbeat traffic mirror')
            }
        ],
    #DryRun = True
    )
    #print("Create Traffic Mirror Target response is ")
    debugprint(response)
    return(response)
    
def create_traffic_mirror_filter():
    ec2 = boto3.client('ec2')
    response = ec2.create_traffic_mirror_filter(
        Description = 'bhayes-winlogbeat-zeek-mirror-filter',
        TagSpecifications=[
            {
                'ResourceType': 'traffic-mirror-filter',
                'Tags': make_tags('winlogbeat traffic mirror filter')
            }
        ]   
    )
    debugprint(response)
    TrafficMirrorFilter = response['TrafficMirrorFilter']
    filter_id = TrafficMirrorFilter['TrafficMirrorFilterId']
    print("filter ID is " + filter_id)
    return(filter_id)
    
    
def create_traffic_mirror_filter_rule(filter_id):
    ec2 = boto3.client('ec2')
    ingress_response = ec2.create_traffic_mirror_filter_rule(
        TrafficMirrorFilterId = filter_id,
        TrafficDirection = 'ingress',
        RuleNumber = 100,
        RuleAction = 'accept',
        SourceCidrBlock = '0.0.0.0/0',
        DestinationCidrBlock = '10.100.0.0/24',
        Description = 'Default Allow All'
    )
    print("create ingress rule")
    debugprint(json.dumps(ingress_response))

    ec2 = boto3.client('ec2')
    egress_response = ec2.create_traffic_mirror_filter_rule(
        TrafficMirrorFilterId = filter_id,
        TrafficDirection = 'egress',
        RuleNumber = 100,
        RuleAction = 'accept',
        SourceCidrBlock = '10.100.0.0/24',
        DestinationCidrBlock = '0.0.0.0/0',
        Description = 'Default Allow All'           
    )
    print("create egress rule")
    debugprint(json.dumps(egress_response))
    

    
def create_traffic_mirror_session(windows_nic_id,TargetId,filter_id):
    myfigs = parse_configs()
    Owner_Name = myfigs['Owner_Name']
    Owner_Email = myfigs['Owner_Email']
    demo_name = myfigs['demo_name']    
    
    ec2 = boto3.client('ec2')
    response = ec2.create_traffic_mirror_session(
        NetworkInterfaceId = windows_nic_id,
        TrafficMirrorTargetId = TargetId,
        TrafficMirrorFilterId = filter_id,
        SessionNumber = 1,
        Description = 'bhayes winlogbeat demo mirror session',
        TagSpecifications=[
            {
                'ResourceType': 'traffic-mirror-session',
                'Tags': make_tags('winlogbeat traffic mirror session')
            }
        ]        
    )
    debugprint(response)
    return(response)

    
if args.debug:
	def debugprint(*args):
		for arg in args:
			print (arg),
		print
		print
else:
	debugprint = lambda *a: None

zeek_instance_info = get_running_instance_info('zeek')
debugprint(zeek_instance_info)
zeek_host_info = parse_instance_info(zeek_instance_info)
debugprint(json.dumps(zeek_host_info))

zeek_nic_id = zeek_host_info['NetworkInterfaceId']


windows_instance_info = get_running_instance_info('windows')
windows_host_info = parse_instance_info(windows_instance_info)
windows_nic_id = windows_host_info['NetworkInterfaceId']

debugprint(json.dumps(windows_host_info))

filter_id = create_traffic_mirror_filter()
create_traffic_mirror_filter_rule(filter_id)


mirror_target_response = create_traffic_mirror_target(zeek_nic_id)
TrafficMirrorTarget = mirror_target_response['TrafficMirrorTarget']
TargetId = TrafficMirrorTarget['TrafficMirrorTargetId']
print("Created Traffic Mirror Target ID " + TargetId)

mirror_session = create_traffic_mirror_session(windows_nic_id,TargetId,filter_id)
if mirror_session['TrafficMirrorSession']:
    TrafficMirrorSession = mirror_session['TrafficMirrorSession']
    TrafficMirrorSessionId = TrafficMirrorSession['TrafficMirrorSessionId']
    print("traffic mirror session " + TrafficMirrorSessionId + " created")
    
else:
    exit("thing bad - I amd slain")
