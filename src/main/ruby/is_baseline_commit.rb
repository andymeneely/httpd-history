# Output whether each VCC was the baseline commit for the vulnerable file in
# that commit.
#
# mxm6060@rit.edu

require 'csv'

def main
    index = 1
    path_to_csv = "../httpd-data/googledocs/HTTPD\ Vulnerability\ " +
        "Introduction.csv"
    path_to_httpd = "../httpd"
    Dir.chdir path_to_httpd do
        CSV.foreach(path_to_csv) do |row|
            if row[3] == "N/A"
                puts "#{index} #{row[2]}  N/A"
            else
                `git format-patch -1 #{row[3]}`
                grep = "grep -A 1 -B 0 \"diff --git a/#{row[2]} b/#{row[2]}\""
                grep_result = `cat #{path_to_httpd}/0001-* | #{grep}`
                result = "#{index} #{row[2]}  "
                grep_result.match(/new file mode/) ? result+="Yes" : result+="No"
                puts result
                `rm #{path_to_httpd}/0001-*`
            end
            index += 1
        end
    end
end

if __FILE__ == $0
    main
end
