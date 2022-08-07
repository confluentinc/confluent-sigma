# create_vpc.py
# This script creates a VPC, subnet, Intenet Gateway, etc.

import boto3
from configparser import ConfigParser
import argparse
import json

parser = argparse.ArgumentParser(description=
    '''This script creates a VPC in AWS''')
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
    figs['zeek_ami'] = cfg.get('zeek', 'ami')
    figs['windows_ami'] = cfg.get('windows', 'ami')
    figs['windows_instance_type'] = cfg.get('windows', 'InstanceType')
    figs['zeek_instance_type'] = cfg.get('zeek', 'InstanceType')
    figs['windows_vm_name'] = cfg.get('windows', 'vm_name')
    figs['zeek_vm_name'] = cfg.get('zeek', 'vm_name')
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

def create_vpc():
    ec2 = boto3.client('ec2')
    response = ec2.create_vpc(
        CidrBlock = '10.100.0.0/24',
        AmazonProvidedIpv6CidrBlock = False,
        TagSpecifications = [
            {
                'ResourceType': 'vpc',
                'Tags': make_tags('winlogbeat demo vpc')
            }
            
        ],
        #DryRun = True
    )
    return(response)


def create_subnet(vpc_id):
    ec2 = boto3.client('ec2')
    response = ec2.create_subnet(
        CidrBlock = '10.100.0.0/24',
        VpcId = vpc_id,
        TagSpecifications = [
            {
                'ResourceType': 'subnet',
                'Tags': make_tags('winlogbeat subnet')
            }
        ]          
    )
    debugprint(json.dumps(response))
    Subnet = response['Subnet']
    subnet_id = Subnet['SubnetId']
    return(subnet_id)

def enableDnsHostnames(vpc_id):
    ec2 = boto3.client('ec2')
    response = ec2.modify_vpc_attribute(
        EnableDnsHostnames = {
            'Value': True
        },
        VpcId = vpc_id
    )
    return(response)
    
    
def create_internet_gateway(vpc_id):
    ec2 = boto3.client('ec2')
    create_response = ec2.create_internet_gateway(
        TagSpecifications = [
            {
                'ResourceType': 'internet-gateway',
                'Tags': make_tags('winlogbeat vpc internet gateway')
            }
            
        ]       
    )
    debugprint(json.dumps(create_response))
    InternetGateway = create_response['InternetGateway']
    gateway_id = InternetGateway['InternetGatewayId']    

    ec2 = boto3.client('ec2')
    attach_response = ec2.attach_internet_gateway(
        InternetGatewayId = gateway_id,
        VpcId = vpc_id
    )
    debugprint(json.dumps(attach_response))
    return(gateway_id)


def route_table_info(vpc_id):
    ec2 = boto3.client('ec2')
    table_filters = [
        {'Name': 'vpc-id', 'Values': [vpc_id]}
    ]
    response = ec2.describe_route_tables(Filters=table_filters)
    RouteTables = response['RouteTables']
    table = RouteTables[0]
    RouteTableId = table['RouteTableId']
    debugprint(json.dumps(response))
    return(RouteTableId)


def create_default_route(route_id,gateway_id,subnet_id):
    ec2 = boto3.client('ec2')
    response = ec2.create_route(
        DestinationCidrBlock = '0.0.0.0/0',
        RouteTableId = route_id,
        GatewayId = gateway_id
    )
    debugprint(json.dumps(response))

    ec2 = boto3.client('ec2')
    associate_response = ec2.associate_route_table(
        RouteTableId = route_id,
        SubnetId = subnet_id
    )
    debugprint(json.dumps(associate_response))
    
    
def create_security_group(vpc_id):
    ec2 = boto3.client('ec2')
    response = ec2.create_security_group(
        Description = 'security group for winlogbeat demo',
        GroupName = 'bhayes winlogbeat security group',
        VpcId = vpc_id,
        TagSpecifications = [
            {
                'ResourceType': 'security-group',
                'Tags': make_tags('winlogbeat security group')
            }
        ]          
    )
    GroupId = response['GroupId']
    return(GroupId)


def authorize_security_group_ingress(GroupId):
    ec2 = boto3.client('ec2')
    response = ec2.authorize_security_group_ingress(
        GroupId = GroupId,
        IpPermissions = [
            {
                'IpProtocol': '-1',
                'IpRanges': [
                    {
                        'CidrIp': '0.0.0.0/0',
                        'Description': 'Errawhere'
                    }
                ]
            }
        ]        
    )
    debugprint(response)
    

def create_instance(host_job,subnet,security_group_id):
    figs = parse_configs()
    ec2 = boto3.resource('ec2')
    if host_job == 'zeek':
        ami = figs['zeek_ami']
        InstanceType = figs['zeek_instance_type']
        vm_name = figs['zeek_vm_name']
        vm_name = vm_name + '-zeek'
    elif host_job == 'windows':
        ami = figs['windows_ami']            
        InstanceType = figs['windows_instance_type']
        vm_name = figs['windows_vm_name']
        vm_name = vm_name + '-windows'
    demo_host = host_job
    demo_hostname = host_job 
    SecurityGroupIds = []
    SecurityGroupIds.append(security_group_id)
    print("Creating Instance ", demo_host)
    tags = make_tags(vm_name)
    tags.append({ 'Key': 'demo_host', 'Value': demo_host})
    ec2.create_instances(
        # DryRun=True,
        BlockDeviceMappings=[
            {
                'DeviceName': '/dev/sda1',
                'Ebs': {
                    'DeleteOnTermination': True,
                    'VolumeSize': 250,
                    'VolumeType': 'gp2',
                    'Encrypted': False
                }
            }
        ],
        ImageId=ami,
        MinCount=1,
        MaxCount=1,
        InstanceType=InstanceType,
        KeyName=figs['pem'],
        NetworkInterfaces=[
            {
                'AssociatePublicIpAddress': True,
                'DeviceIndex': 0,
                'Ipv6AddressCount': 0,
                'Description': 'primary NIC',
                'SubnetId': subnet,
                'Groups': SecurityGroupIds
                }
            ],
        TagSpecifications=[
            {
                'ResourceType': 'instance',
                'Tags': tags
            }
        ]
    )
    print(vm_name, " has been created")


############### done with function definitions
############### make stuff happen here

if args.debug:
	def debugprint(*args):
		for arg in args:
			print (arg),
		print
		print
else:
	debugprint = lambda *a: None

vpc_response = create_vpc()
#print(json.dumps(vpc_response))
if vpc_response:
    Vpc = vpc_response['Vpc']
    vpc_id = Vpc['VpcId']
    print("Created VPC " + vpc_id)

subnet_id = create_subnet(vpc_id)
print ("Created subnet " + subnet_id)

dns_repsonse = enableDnsHostnames(vpc_id)
debugprint(json.dumps(dns_repsonse))

gateway_id = create_internet_gateway(vpc_id)
print("Created Internet Gateway " + gateway_id)

main_route_table_id = route_table_info(vpc_id)
route_response = create_default_route(main_route_table_id,gateway_id,subnet_id)
debugprint(json.dumps(route_response))

security_group_id = create_security_group(vpc_id)
print("Created Security Group " + security_group_id)
authorize_security_group_ingress(security_group_id)

create_instance('zeek',subnet_id,security_group_id)

create_instance('windows', subnet_id,security_group_id)
    