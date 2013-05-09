# Fetches the raw bug reports and comments from Apache's legacy bug reporting
# system, gnats. Bugs are from 1996 to March 18, 2002, when they moved to
# bugzilla.
#
# gnats:    http://archive.apache.org/gnats
# bugzilla: https://issues.apache.org/bugzilla
#
# mxm6060@rit.edu

PR_NUMBERS = (1 .. 10295)

def main
    PR_NUMBERS.each do |pr|
        puts "Fetching PR: #{pr}"
        url = "http://archive.apache.org/gnats/#{pr}"
        `wget -o /dev/null -O - #{url} >> raw-gnats-archive.txt`
    end
end

if __FILE__ == $0
    main
end
