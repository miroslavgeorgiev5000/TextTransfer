# TextTransfer

A multi user text editor. Allows you to have multiple people edit the same text in real time.
Works via unverified UDP, currently unstable/not fit for actual work.

Usage Instructions:
1. Download
2. execute compile_cl.bat and compile_sv.bat
3. execute run_sv.bat to run the server
4. execute run_cl.bat twice for both client windows
5. type into one of the client windows and observe the changes in the other one.

- if you want to run the server remotely, don't forget to forward port 4445 on the server machine's network
- to connect a client to a remote server, change the IP address in connect_address.txt file
