use IO::Socket;
my $sock = IO::Socket::INET->new('some_server');
#$sock->read($data, 1024) until $sock->atmark;

my %keys;

sub crear {
    my $nombre    = shift @_;
    my $dimension = shift @_;
    my $tipo      = shift @_;
    my @lista     = @_;

    if ($dimension != 2) {
        return;
    }
    
    $keys{$nombre} = @lista;

    print $nombre . "\n";
    print $dimension . "\n";
    print $tipo . "\n";
    print join( ", ", @lista) . "\n";
    print $hash{$nombre} . "\n";
}



crear("Fabiola", 10, "Linda", ('A','B','C'));
