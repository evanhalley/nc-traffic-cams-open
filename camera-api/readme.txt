http://iconof.com/blog/how-to-install-setup-node-js-on-amazon-aws-ec2-complete-guide/
AWS Setup
1-sudo nano /etc/yum.repos.d/epel.repo
2-change enabled from 0 to 1 for [epel]
3-sudo yum update
4-sudo yum install npm

Mongo
#!/bin/bash
echo "*****************************************"
echo " Add the 10gen repository - after you press"
echo " enter add the following lines and then"
echo " cntl-X to save:"
echo " [10gen]"
echo " name=10gen Repository"
echo " baseurl=http://downloads-distro.mongodb.org/repo/redhat/os/x86_64"
echo " gpgcheck=0"
echo "*****************************************"
read -p "Press [Enter] to continue..."
nano /etc/yum.repos.d/10gen.repo

# install MongoDB
sudo yum install mongo-10gen and mongo-10gen-server

# start the service
sudo service mongod start

# install service
sudo chkconfig mongod on


