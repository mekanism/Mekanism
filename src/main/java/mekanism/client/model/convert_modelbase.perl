#!/usr/bin/env perl
# Released into Public Domain

# A quick and dirty script to convert ModelBase Java code as generated
# by Techne into Minecraft's JSON model format.
# It is quite limited (it does not handle rotations), but it is at least a help.

# Note that Minecraft requires the texure to be square, so you might need to
# resize your texture

use strict;
use warnings;
use List::Util qw[max];

my %parts;

while(<>) {
    if(/\s*(\S+) = new ModelRenderer\(this, (\d+), (\d+)\);/) {
        $parts{$1}{base_uv} = [$2, $3];
    }
    elsif(/\s*(\S+)\.addBox\((-?\d*\.?\d*)[Ff]?, (-?\d*\.?\d*)[Ff]?, (-?\d*\.?\d*)[Ff]?, (\d+), (\d+), (\d+)\);/) {
        $parts{$1}{offset} = [$2, $3, $4];
        $parts{$1}{size} = [$5, $6, $7];
    }
    elsif(/\s*(\S+)\.setRotationPoint\((-?\d*\.?\d*)[Ff]?, (-?\d*\.?\d*)[Ff]?, (-?\d*\.?\d*)[Ff]?\);/) {
        $parts{$1}{rot_point} = [$2, $3, $4];
    }
    elsif(/\s*(\S+)\.setTextureSize\((\d+), (\d+)\);/) {
        $parts{$1}{tex_size} = [$2, $3];
    }
    elsif(/\s*(\S+)\.mirror = (true|false);/) {
        $parts{$1}{mirror} = ($2 eq "true");
    }
    elsif(/\s*setRotation\((\S+), (-?\d*\.?\d*)[Ff]?, (-?\d*\.?\d*)[Ff]?, (-?\d*\.?\d*)[Ff]?\)/) {
        #$parts{$1}{rotation} = [$2, $3, $4];
        #We ignore the rotation
    }
    else {
        #print STDERR "Ignoring line $.\n";
    }
}

print "{
    \"elements\": [
";

my $first = 1;

foreach my $part (keys %parts) {
    my $offset = $parts{$part}{offset};
    my $size = $parts{$part}{size};
    my $rot_point = $parts{$part}{rot_point};
    #Textures need to be square
    my $tex_size = max($parts{$part}{tex_size}->[0], $parts{$part}{tex_size}->[1]);
    my $base_uv = $parts{$part}{base_uv};

    my $xmin = 8 + $offset->[0] + $rot_point->[0];
    my $ymin = 23 - $offset->[1] - $size->[1] - $rot_point->[1];
    my $zmin = 8 + $offset->[2] + $rot_point->[2];

    my $xmax = 8 + $offset->[0] + $size->[0] + $rot_point->[0];
    my $ymax = 23 - $offset->[1] - $rot_point->[1];
    my $zmax = 8 + $offset->[2] + $size->[2] + $rot_point->[2];

    my @uvmin = (
        [$base_uv->[0] +   $size->[2] + $size->[0], $base_uv->[1]],               #down
        [$base_uv->[0] +   $size->[2],              $base_uv->[1]],               #up
        [$base_uv->[0] +   $size->[2],              $base_uv->[1] + $size->[2]],  #north
        [$base_uv->[0] + 2*$size->[2] + $size->[0], $base_uv->[1] + $size->[2]],  #south
        [$base_uv->[0] +   $size->[2] + $size->[0], $base_uv->[1] + $size->[2]],  #west
        [$base_uv->[0],                             $base_uv->[1] + $size->[2]]); #east

    my @uvmax = (
        [$uvmin[0][0] + $size->[0], $uvmin[0][1] + $size->[2]],  #down
        [$uvmin[1][0] + $size->[0], $uvmin[1][1] + $size->[2]],  #up
        [$uvmin[2][0] + $size->[0], $uvmin[2][1] + $size->[1]],  #north
        [$uvmin[3][0] + $size->[0], $uvmin[3][1] + $size->[1]],  #south
        [$uvmin[4][0] + $size->[2], $uvmin[4][1] + $size->[1]],  #west
        [$uvmin[5][0] + $size->[2], $uvmin[5][1] + $size->[1]]); #east

    #Scale UVs from 0 to 16 with respect to texture size
    foreach my $f (@uvmin) {
        foreach (@$f) {
            $_ = $_ / $tex_size * 16;
        }
    }
    foreach my $f (@uvmax) {
        foreach (@$f) {
            $_ = $_ / $tex_size * 16;
        }
    }

    print ",\n" unless $first;
    $first = 0;

    print
"        {
            \"__comment\": \"$part\",
            \"from\": [$xmin, $ymin, $zmin],
            \"to\": [$xmax, $ymax, $zmax],
            \"faces\": {
                \"down\":  {\"uv\": [$uvmin[0][0], $uvmin[0][1], $uvmax[0][0], $uvmax[0][1]], \"texture\": \"#all\"},
                \"up\":    {\"uv\": [$uvmin[1][0], $uvmin[1][1], $uvmax[1][0], $uvmax[1][1]], \"texture\": \"#all\"},
                \"north\": {\"uv\": [$uvmin[2][0], $uvmin[2][1], $uvmax[2][0], $uvmax[2][1]], \"texture\": \"#all\"},
                \"south\": {\"uv\": [$uvmin[3][0], $uvmin[3][1], $uvmax[3][0], $uvmax[3][1]], \"texture\": \"#all\"},
                \"west\":  {\"uv\": [$uvmin[4][0], $uvmin[4][1], $uvmax[4][0], $uvmax[4][1]], \"texture\": \"#all\"},
                \"east\":  {\"uv\": [$uvmin[5][0], $uvmin[5][1], $uvmax[5][0], $uvmax[5][1]], \"texture\": \"#all\"}
            }
        }";
}

print "
    ],
    \"textures\": {
        \"all\": \"...\",
        \"particle\": \"...\"
    }
}
";

