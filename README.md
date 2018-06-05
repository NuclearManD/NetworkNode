# NetworkNode

## What is it?
This repo is meant to work as the internet "glue" for a network I am building, although it has becomemore of a blockchain client and node.

~~The project manages a NuclearBlocks fork called Freechain that works as a database with no currency.~~

~~Additionally this project is meant to work with radio modules to provide network connection to electronic devices without wifi or cell service.~~
 > That will be done with other devices interfacing with this software.
 
 This node also serves as a webserver for websites on the blockchain.  This feature is still quite beta.

## How do I use it?


Download the latest NTI-Newtork jarfile.  The jar takes command line arguments, summed up as follows:

 * -w starts the blockchain client
 * -n starts the node
 * -d starts the blockchain web development tool (SUPER BETA)
 
Any combination of these programs can be started at once.  Example:

`java -jar NTI-Network-2.0.1.jar -w -n`

That command will start both the node and the blockchain client at once.

Currently there are no stable jars for any of these tools alone.

## How do I start my own node?

I am almost done with making the network support multiple nodes.  On the other hand, I will not be able to test this code for a few days because making a new node costs 1000 coins, and I have not verified enough blocks to have that much of the currency.

*In short*, contact me if you would like to start a node for this blockchain.  I will be thrilled and be very willing to help you :-)

