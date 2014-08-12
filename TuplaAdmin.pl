e IO::Socket;
my $sock = IO::Socket::INET->new('some_server');
$sock->read($data, 1024) until $sock->atmark;
